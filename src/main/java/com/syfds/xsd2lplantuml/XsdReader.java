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
        List<Element> elements = new ArrayList<>();
        List<Relation> relationships = new ArrayList<>();

        parser.getResultXsdElements().forEach(xsdElement -> {
            Element element = mapElement(xsdElement);
            elements.add(element);

            element.getAttributeList().forEach(attr -> {
                if (attr.getXsdType() != null) {
                    Element subElement = mapElement(attr.getXsdType());
                    elements.add(subElement);
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

        return new EntityRelationshipModel(elements, relationships);
    }


    private Element mapElement(XsdElement xsdElement) {
        Element element = new Element(xsdElement.getName());
        if (xsdElement.getXsdComplexType() != null) {
            XsdComplexType xsdComplexType = xsdElement.getXsdComplexType();
            element.setComment(getDocumentation(xsdComplexType));
            xsdComplexType.getElements().forEach(elem -> {
                mapAttribute(elem, element);
            });

            xsdComplexType.getXsdAttributes().forEach(xsdAttribute -> {
                Attribute attr = new Attribute(xsdAttribute.getName(), xsdAttribute.getType(), getDocumentation(xsdAttribute));
                element.addAttribute(attr);
            });
        }
        return element;
    }

    private static void mapAttribute(ReferenceBase attribute, Element element) {
        if (attribute instanceof NamedConcreteElement namedElement) {
            mapNamedConcreteElement(namedElement, element);
        } else {
            throw new IllegalArgumentException("Unsupported element type: " + attribute.getClass().getName());
        }
    }

    private static void mapNamedConcreteElement(NamedConcreteElement namedElement, Element element) {
        XsdElement element1 = (XsdElement) namedElement.getElement();
        Attribute attr = new Attribute(namedElement.getName(), element1.getType(), getDocumentation(namedElement.getElement()));

        if (element1.getXsdComplexType() != null) {
            attr.setXsdType(element1);
        }
        element.addAttribute(attr);
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
