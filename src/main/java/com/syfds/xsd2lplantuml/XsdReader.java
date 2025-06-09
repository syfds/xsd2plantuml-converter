package com.syfds.xsd2lplantuml;

import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.elementswrapper.NamedConcreteElement;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class XsdReader {

    public EntityRelationshipModel mapToModel(String filepath) {
        XsdParser parser = new XsdParser(filepath);
        return map(parser);
    }

    public EntityRelationshipModel map(XsdParser parser) {
        List<Entity> entities = new ArrayList<>();
        List<Relation> relationships = new ArrayList<>();

        parser.getResultXsdElements().forEach(xsdElement -> {
            Entity entity = mapToEntity(xsdElement);
            entities.add(entity);

            Optional.ofNullable(entity.getAttributeList())
                    .ifPresent(attributes -> {
                        attributes.forEach(attr -> {
                            if (attr.isComplexType()) {
                                relationships.add(new Relation(entity.getUniqueName(), attr.getType(), attr.getRelationType()));

                                XsdComplexType resolvedType = findComplexTypeByName(parser, attr.getType());
                                Entity complextTypeAsEntity = mapToEntity(resolvedType);
                                entities.add(complextTypeAsEntity);

                                addRelations(complextTypeAsEntity, relationships);
                            }
                        });
                    });


            if (entity.getType() != null) {
                relationships.add(new Relation(entity.getUniqueName(), entity.getType(), RelationType.TYPE_OF));
                XsdComplexType resolvedType = findComplexTypeByName(parser, entity.getType());
                Entity complextTypeAsEntity = mapToEntity(resolvedType);
                entities.add(complextTypeAsEntity);
                addRelations(complextTypeAsEntity, relationships);
            }
//            if (xsdElement.getXsdComplexType() != null) {
//                XsdComplexType complexType = xsdElement.getXsdComplexType();
//                complexType.getAllXsdAttributes().forEach(child -> {
//                    Relation relation = new Relation(element, mapAttribute(child), RelationType.ONE_TO_ONE);
//                    relationships.add(relation);
//                });
//            }
        });

        return new EntityRelationshipModel(entities, relationships);
    }

    private static void addRelations(Entity complextTypeAsEntity, List<Relation> relationships) {
        Optional.ofNullable(complextTypeAsEntity.getAttributeList())
                .ifPresent(attributes -> {
                    attributes.forEach(attr -> {
                        if (attr.isComplexType()) {
                            relationships.add(new Relation(complextTypeAsEntity.getUniqueName(), attr.getType(), attr.getRelationType()));
                        }
                    });
                });
    }

    private Entity mapToEntity(XsdComplexType resolvedType) {
        Entity entity = new Entity(resolvedType.getName());
        entity.setComment(getDocumentation(resolvedType));
        resolvedType.getElements().forEach(elem -> {
            mapAttribute(elem, entity);
        });

        resolvedType.getXsdAttributes().forEach(xsdAttribute -> {
            Attribute attr = new Attribute(xsdAttribute.getName(), xsdAttribute.getType(), getDocumentation(xsdAttribute));
            entity.addAttribute(attr);
        });

        return entity;
    }

    private XsdComplexType findComplexTypeByName(XsdParser parser, String typeName) {
        return parser.getResultXsdSchemas()
                .flatMap(XsdSchema::getXsdElements)
                .filter(element -> element instanceof XsdComplexType)
                .map(element -> (XsdComplexType) element)
                .filter(complexType -> complexType.getName().equals(typeName))
                .findFirst().orElseThrow();
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
            throw new IllegalArgumentException("Unsupported element type: " + attribute.getClass().getName());
        }
    }

    private static void mapNamedConcreteElement(NamedConcreteElement namedElement, Entity entity) {
        XsdElement xsdElement = (XsdElement) namedElement.getElement();
        Attribute attr = new Attribute(namedElement.getName(), xsdElement.getType(), getDocumentation(namedElement.getElement()));
        attr.setComplexType(xsdElement.getXsdComplexType() != null);
        attr.setRelationType(xsdElement.getMinOccurs(), xsdElement.getMaxOccurs());
        entity.addAttribute(attr);
    }

    private static String getDocumentation(XsdNamedElements element) {
        XsdAnnotation annotation = element.getAnnotation();
        return annotation != null ? (getContent(annotation) != null ? getContent(annotation) : null) : null;
    }

    private static String getContent(XsdAnnotation annotation) {
        return annotation.getDocumentations().stream().map(XsdDocumentation::getContent)
                .filter(content -> content != null && !content.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
