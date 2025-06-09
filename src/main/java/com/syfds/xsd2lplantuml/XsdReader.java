package com.syfds.xsd2lplantuml;

import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.elementswrapper.NamedConcreteElement;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class XsdReader {

    public EntityRelationshipModel readXsd(String filepath) {
        XsdParser parser = new XsdParser(filepath);
        return map(parser);
    }

    public EntityRelationshipModel map(XsdParser parser) {
        List<Entity> entities = new ArrayList<>();
        List<Relation> relationships = new ArrayList<>();

        parser.getResultXsdElements().forEach(xsdElement -> {
            Entity entity = mapElement(xsdElement);
            entities.add(entity);

            entity.getAttributeList().forEach(attr -> {
                if (attr.getXsdType() != null) {
                    Entity subEntity = mapElement(attr.getXsdType());
                    entities.add(subEntity);
                }
            });
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


    private Entity mapElement(XsdElement xsdElement) {
        Entity entity = new Entity(xsdElement.getName());
        if (xsdElement.getXsdComplexType() != null) {
            XsdComplexType xsdComplexType = xsdElement.getXsdComplexType();
            entity.setComment(getDocumentation(xsdComplexType));
            xsdComplexType.getElements().forEach(elem -> {
                mapAttribute(elem, entity);
            });

            xsdComplexType.getXsdAttributes().forEach(xsdAttribute -> {
                Attribute attr = new Attribute(xsdAttribute.getName(), xsdAttribute.getType(), getDocumentation(xsdAttribute));
                entity.addAttribute(attr);
            });
        }
        return entity;
    }

    private static void mapAttribute(ReferenceBase attribute, Entity entity) {
        if (attribute instanceof NamedConcreteElement namedElement) {
            mapNamedConcreteElement(namedElement, entity);
        } else {
            throw new IllegalArgumentException("Unsupported element type: " + attribute.getClass().getName());
        }
    }

    private static void mapNamedConcreteElement(NamedConcreteElement namedElement, Entity entity) {
        XsdElement element1 = (XsdElement) namedElement.getElement();
        Attribute attr = new Attribute(namedElement.getName(), element1.getType(), getDocumentation(namedElement.getElement()));

        if (element1.getXsdComplexType() != null) {
            attr.setXsdType(element1);
        }
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
