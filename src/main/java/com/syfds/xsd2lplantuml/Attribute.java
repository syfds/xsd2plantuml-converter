package com.syfds.xsd2lplantuml;

import org.xmlet.xsdparser.xsdelements.XsdComplexType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Attribute {
    private static final String DEFAULT_TYPE_XSD = "xs:anyType";
    private final String name;
    private final String type;

    private boolean isComplexType = false;
    private XsdComplexType internalComplexType;
    private final String comment;
    private RelationType relationType;
    private boolean simpleType;
    private boolean isEnumeration = false;
    private List<String> enumValues = new ArrayList<>();

    public Attribute(String name, String type, String comment) {
        this.name = name;

        boolean typeSet = type != null;
        this.type = typeSet ? type : DEFAULT_TYPE_XSD;
        if (!typeSet)
            System.out.println("Warning: Type for attribute '" + name + "' is not set, using default type '" + DEFAULT_TYPE_XSD + "'.");

        this.comment = comment;
    }

    public Attribute(String name, String type, String comment, List<String> enumValues) {
        // constructor for enumeration attributes
        this(name, type, comment);
        this.enumValues = enumValues;
        this.isEnumeration = true;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public boolean isEnumeration() {
        return isEnumeration;
    }

    public void setEnumeration(boolean enumeration) {
        isEnumeration = enumeration;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isComplexType() {
        return isComplexType;
    }

    public void setComplexType(boolean complexType) {
        isComplexType = complexType;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public void setRelationType(Integer minOccurs, String maxOccurs) {
        if ((minOccurs == 0 || minOccurs == 1) && !Objects.equals(maxOccurs, "unbounded")) {
            setRelationType(RelationType.ONE_TO_ONE);
        } else if ((minOccurs == 0 || minOccurs == 1) && Objects.equals(maxOccurs, "unbounded")) {
            setRelationType(RelationType.ONE_TO_MANY);
        }
    }

    public void setInternalComplexType(XsdComplexType internalComplexType) {
        this.internalComplexType = internalComplexType;
    }

    public XsdComplexType getInternalComplexType() {
        return internalComplexType;
    }

    public void setSimpleType(boolean simpleType) {
        this.simpleType = simpleType;
    }

    public boolean isSimpleType() {
        return simpleType;
    }
}
