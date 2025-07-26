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

        processNotReferenceComplexTypes(parser, entities, relationships);

        return new EntityRelationshipModel(entities, relationships);
    }

    private void processNotReferenceComplexTypes(XsdParser parser, Set<Entity> entities, Set<Relation> relationships) {
        parser.getResultXsdSchemas().flatMap(XsdSchema::getXsdElements).filter(element -> element instanceof XsdComplexType).map(element -> (XsdComplexType) element).filter(complexType -> !entities.contains(new Entity(complexType.getName()))).forEach(notYetHandledComplexType -> {
            Entity complextTypeAsEntity = mapToEntity(notYetHandledComplexType);
            entities.add(complextTypeAsEntity);
            createEntitiesFromAttributesRecursively(parser, complextTypeAsEntity, relationships, entities);
            if (complextTypeAsEntity.getType() != null) {
                resolveType(complextTypeAsEntity, parser, relationships, entities);
            }
        });
    }

    private void resolveType(Entity entity, XsdParser parser, Set<Relation> relationships, Set<Entity> entities) {
        relationships.add(new Relation(entity.getUniqueName(), entity.getType(), entity.getRelationType()));
        XsdComplexType resolvedType = findComplexTypeByName(parser, entity.getType());
        Entity complextTypeAsEntity = mapToEntity(resolvedType);
        entities.add(complextTypeAsEntity);
        createEntitiesFromAttributesRecursively(parser, complextTypeAsEntity, relationships, entities);
    }

    private void createEntitiesFromAttributesRecursively(XsdParser parser, Entity entity, Set<Relation> relationships, Set<Entity> allEntities) {
        Optional.ofNullable(entity.getAttributeList()).ifPresent(attributes -> {
            attributes.forEach(attr -> {
                if (attr.isComplexType()) {

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
                        boolean isNewEntity = allEntities.add(complextTypeAsEntity);
                        if (isNewEntity) {
                            createEntitiesFromAttributesRecursively(parser, complextTypeAsEntity, relationships, allEntities);
                        }
                    }
                    relationships.add(new Relation(entity.getUniqueName(), attr.getType(), attr.getRelationType()));
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

        if (resolvedType.getComplexContent() != null) {
            Optional.ofNullable(resolvedType.getComplexContent().getXsdExtension().getElements()).ifPresent(elems -> {
                elems.forEach(elem -> {
                    mapAttribute(elem, entity);
                });
            });
            entity.setExtensionType(resolvedType.getComplexContent().getXsdExtension().getBase().getName());
        }

        return entity;
    }

    private Entity mapToEntity(XsdSimpleType resolvedType) {
        Entity entity = new Entity(resolvedType.getName() != null ? resolvedType.getName() : ((XsdNamedElements) resolvedType.getParent()).getName());
        entity.setComment(getDocumentation(resolvedType));
        resolvedType.getElements().forEach(elem -> {
            mapAttribute(elem, entity);
        });


        if (resolvedType.getRestriction() != null) {
            Attribute attr = createAttributeForRestriction(resolvedType);
            entity.addAttribute(attr);
        }

        return entity;
    }

    private static Attribute createAttributeForRestriction(XsdSimpleType resolvedType) {
        StringBuilder restrictions = new StringBuilder();
        if (resolvedType.getRestriction().getMinLength() != null) {
            restrictions.append("minLength=").append(resolvedType.getRestriction().getMinLength().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getMaxLength() != null) {
            restrictions.append("maxLength=").append(resolvedType.getRestriction().getMaxLength().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getPattern() != null) {
            restrictions.append("pattern=").append(resolvedType.getRestriction().getPattern().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getWhiteSpace() != null) {
            restrictions.append("whiteSpace=").append(resolvedType.getRestriction().getWhiteSpace().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getTotalDigits() != null) {
            restrictions.append("totalDigits=").append(resolvedType.getRestriction().getTotalDigits().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getFractionDigits() != null) {
            restrictions.append("fractionDigits=").append(resolvedType.getRestriction().getFractionDigits().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getLength() != null) {
            restrictions.append("length=").append(resolvedType.getRestriction().getLength().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getMaxInclusive() != null) {
            restrictions.append("maxInclusive=").append(resolvedType.getRestriction().getMaxInclusive().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getMaxExclusive() != null) {
            restrictions.append("maxExclusive=").append(resolvedType.getRestriction().getMaxExclusive().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getMinInclusive() != null) {
            restrictions.append("minInclusive=").append(resolvedType.getRestriction().getMinInclusive().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getMinExclusive() != null) {
            restrictions.append("minExclusive=").append(resolvedType.getRestriction().getMinExclusive().getValue()).append(", ");
        }
        if (resolvedType.getRestriction().getBase() != null) {
            restrictions.append("base=").append(resolvedType.getRestriction().getBase()).append(", ");
        }

        if (resolvedType.getRestriction().getEnumeration() != null && !resolvedType.getRestriction().getEnumeration().isEmpty()) {
            String enumValues = resolvedType.getRestriction().getEnumeration().stream().map(XsdStringRestrictions::getValue).collect(Collectors.joining(", "));
            restrictions.append("enumeration={").append(enumValues).append("}, ");
        }
        if (!restrictions.isEmpty()) {
            restrictions.setLength(restrictions.length() - 2); // remove last comma and space
        }

        String documentation = getDocumentation(resolvedType.getRestriction());
        if (documentation == null) {
            documentation = "";
        }

        return new Attribute("restriction", "restriction", documentation + " (" + restrictions + ")");
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

            if (attribute.getElement() instanceof XsdChoice choice) {
                if (choice.getElements() != null) {
                    choice.getElements().forEach(elem -> {
                        if (elem instanceof NamedConcreteElement elemNamed) {
                            mapAttribute(elemNamed, entity);
                        }
                    });
                } else {
                    System.out.println("Choice element without elements found, will be skipped.");
                }
            } else if (attribute.getElement() != null) {
                System.out.println("Unsupported element type: " + attribute.getElement().getClass() + " with attributes" + " " + attribute.getElement().getAttributesMap() + ", will be skipped.");
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
