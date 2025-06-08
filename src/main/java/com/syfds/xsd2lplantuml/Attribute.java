package com.syfds.xsd2lplantuml;

import org.xmlet.xsdparser.xsdelements.XsdElement;

public class Attribute {
    private String name;
    private String type;
    private XsdElement xsdType;
    private String comment;

    public Attribute(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    public void setXsdType(XsdElement xsdType) {
        this.xsdType = xsdType;
    }

    public XsdElement getXsdType() {
        return xsdType;
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
                ", xsdType=" + xsdType +
                ", comment='" + comment + '\'' +
                '}';
    }
}
