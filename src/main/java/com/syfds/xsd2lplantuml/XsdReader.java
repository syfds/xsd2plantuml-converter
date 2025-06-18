package com.syfds.xsd2lplantuml;

import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ConcreteElement;
import org.xmlet.xsdparser.xsdelements.elementswrapper.NamedConcreteElement;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdStringRestrictions;

import java.util.*;
import java.util.stream.Collectors;


public class XsdReader {

    public EntityRelationshipModel mapToModel(String filepath) {
        XsdParser parser = new XsdParser(filepath);
        return map(parser);
    }

    public EntityRelationshipModel map(XsdParser parser) {
        Set<Entity> entities = new HashSet<>();
        Set<Relation> relationships = new HashSet<>();

        parser.getResultXsdElements().forEach(xsdElement -> {
            Entity entity = mapToEntity(xsdElement);
            entities.add(entity);

            createEntitiesFromAttributesRecursively(parser, entity, relationships, entities);

            if (entity.getType() != null) {
                resolveType(entity, parser, relationships, entities);
            }
        });

        return new EntityRelationshipModel(entities, relationships);
    }

    private void resolveType(Entity entity, XsdParser parser, Set<Relation> relationships, Set<Entity> entities) {
        relationships.add(new Relation(entity.getUniqueName(), entity.getType(), RelationType.TYPE_OF));
        XsdComplexType resolvedType = findComplexTypeByName(parser, entity.getType());
        Entity complextTypeAsEntity = mapToEntity(resolvedType);
        entities.add(complextTypeAsEntity);
        createEntitiesFromAttributesRecursively(parser, complextTypeAsEntity, relationships, entities);
    }

    private void createEntitiesFromAttributesRecursively(XsdParser parser, Entity entity, Set<Relation> relationships, Set<Entity> allEntities) {
        Optional.ofNullable(entity.getAttributeList()).ifPresent(attributes -> {
            attributes.forEach(attr -> {
                if (attr.isComplexType()) {
                    relationships.add(new Relation(entity.getUniqueName(), attr.getType(), attr.getRelationType()));
                    if (attr.getInternalComplexType() != null) {
                        Entity extraEntityForInnerComplexType = mapToEntity(attr.getInternalComplexType());
                        allEntities.add(extraEntityForInnerComplexType);
                        createEntitiesFromAttributesRecursively(parser, extraEntityForInnerComplexType, relationships, allEntities);
                        if (extraEntityForInnerComplexType.getType() != null) {
                            resolveType(extraEntityForInnerComplexType, parser, relationships, allEntities);
                        }
                    } else {
                        XsdComplexType resolvedType = findComplexTypeByName(parser, attr.getType());
                        Entity complextTypeAsEntity = mapToEntity(resolvedType);
                        allEntities.add(complextTypeAsEntity);
                        createEntitiesFromAttributesRecursively(parser, complextTypeAsEntity, relationships, allEntities);
                    }
                } else if (attr.isSimpleType()) {
                    relationships.add(new Relation(entity.getUniqueName(), attr.getType(), attr.getRelationType()));
                    XsdSimpleType simpleTypeByName = findSimpleTypeByName(parser, attr.getType());
                    Entity externAttributeType = mapToEntity(simpleTypeByName);
                    allEntities.add(externAttributeType);
                    createEntitiesFromAttributesRecursively(parser, externAttributeType, relationships, allEntities);
                }
            });
        });
    }

    private static void addRelations(Entity entity, Set<Relation> relationships) {
        Optional.ofNullable(entity.getAttributeList()).ifPresent(attributes -> {
            attributes.forEach(attr -> {
                if (attr.isComplexType()) {
                    relationships.add(new Relation(entity.getUniqueName(), attr.getType(), attr.getRelationType()));
                }
            });
        });
    }

    private Entity mapToEntity(XsdComplexType resolvedType) {
        Entity entity = new Entity(resolvedType.getName() != null ? resolvedType.getName() : ((XsdNamedElements) resolvedType.getParent()).getName());
        entity.setComment(getDocumentation(resolvedType));
        Optional.ofNullable(resolvedType.getElements()).ifPresent((elems) -> {
            elems.forEach(elem -> {
                mapAttribute(elem, entity);
            });
        });

        resolvedType.getXsdAttributes().forEach(xsdAttribute -> {
            Attribute attr = new Attribute(xsdAttribute.getName(), xsdAttribute.getType(), getDocumentation(xsdAttribute));
            entity.addAttribute(attr);
        });

        return entity;
    }

    private Entity mapToEntity(XsdSimpleType resolvedType) {
        Entity entity = new Entity(resolvedType.getName() != null ? resolvedType.getName() : ((XsdNamedElements) resolvedType.getParent()).getName());
        entity.setComment(getDocumentation(resolvedType));
        resolvedType.getElements().forEach(elem -> {
            mapAttribute(elem, entity);
        });


        if (resolvedType.getRestriction() != null) {
            List<String> enumValues = resolvedType.getRestriction().getEnumeration().stream().map(XsdStringRestrictions::getValue).toList();

            Attribute attr = new Attribute("enumeration", "enumeration", getDocumentation(resolvedType.getRestriction()), enumValues);
            entity.addAttribute(attr);
        }

        return entity;
    }

    private XsdComplexType findComplexTypeByName(XsdParser parser, String typeName) {
        return parser.getResultXsdSchemas().flatMap(XsdSchema::getXsdElements).filter(element -> element instanceof XsdComplexType).map(element -> (XsdComplexType) element).filter(complexType -> complexType.getName().equals(typeName)).findFirst().orElseThrow();
    }

    private XsdSimpleType findSimpleTypeByName(XsdParser parser, String typeName) {
        return parser.getResultXsdSchemas().flatMap(XsdSchema::getXsdElements).filter(element -> element instanceof XsdSimpleType).map(element -> (XsdSimpleType) element).filter(complexType -> complexType.getName().equals(typeName)).findFirst().orElseThrow();
    }


    private Entity mapToEntity(XsdElement xsdElement) {
        Entity entity = new Entity(xsdElement.getName());
        if (isInnerComplexType(xsdElement)) {
            XsdComplexType xsdComplexType = xsdElement.getXsdComplexType();
            entity.setComment(getDocumentation(xsdComplexType));
            xsdComplexType.getElements().forEach(elem -> {
                mapAttribute(elem, entity);
            });

            xsdComplexType.getXsdAttributes().forEach(xsdAttribute -> {
                Attribute attr = new Attribute(xsdAttribute.getName(), xsdAttribute.getType(), getDocumentation(xsdAttribute));
                entity.addAttribute(attr);
            });
        } else if (isExternalComplexType(xsdElement)) {
            entity.setType(xsdElement.getType());
        }
        return entity;
    }

    private boolean isExternalComplexType(XsdElement xsdElement) {
        return xsdElement.getType() != null && xsdElement.getXsdComplexType() != null;
    }

    private static boolean isInnerComplexType(XsdElement xsdElement) {
        return xsdElement.getType() == null && xsdElement.getXsdComplexType() != null;
    }

    private static void mapAttribute(ReferenceBase attribute, Entity entity) {
        if (attribute instanceof NamedConcreteElement namedElement) {
            mapNamedConcreteElement(namedElement, entity);
        } else {
            if (attribute.getElement() != null) {
                System.out.println("Unsupported element type: " + attribute.getElement().getClass() + " with attributes" +
                        " " + attribute.getElement().getAttributesMap() + ", will be skipped.");
            }
        }
    }

    private static void mapNamedConcreteElement(NamedConcreteElement namedElement, Entity entity) {
        XsdElement xsdElement = (XsdElement) namedElement.getElement();
        boolean isComplexType = xsdElement.getXsdComplexType() != null;
        boolean isExternalComplexType = isComplexType && xsdElement.getXsdComplexType().getName() != null;
        boolean isSimpleType = xsdElement.getXsdSimpleType() != null;

        boolean isInnerComplexType = isComplexType && !isExternalComplexType;
        Attribute attr;
        if (isInnerComplexType) {
            attr = new Attribute(namedElement.getName(), namedElement.getName(), getDocumentation(namedElement.getElement()));
            attr.setInternalComplexType(xsdElement.getXsdComplexType());
        } else {
            attr = new Attribute(namedElement.getName(), xsdElement.getType(), getDocumentation(namedElement.getElement()));
        }

        attr.setComplexType(isComplexType);
        attr.setSimpleType(isSimpleType);
        attr.setRelationType(xsdElement.getMinOccurs(), xsdElement.getMaxOccurs());
        entity.addAttribute(attr);
    }

    private static String getDocumentation(XsdAnnotatedElements element) {
        XsdAnnotation annotation = element.getAnnotation();
        return annotation != null ? (getContent(annotation) != null ? getContent(annotation) : null) : null;
    }

    private static String getContent(XsdAnnotation annotation) {
        return annotation.getDocumentations().stream().map(XsdDocumentation::getContent).filter(content -> content != null && !content.isEmpty()).collect(Collectors.joining(", "));
    }
}
