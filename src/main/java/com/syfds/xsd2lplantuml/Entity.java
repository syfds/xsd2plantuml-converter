package com.syfds.xsd2lplantuml;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private String uniqueName;
    private String comment;
    private List<Attribute> attributeList;

    public Entity(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Entity(String uniqueName, List<Attribute> attributeList) {
        this.uniqueName = uniqueName;
        this.attributeList = attributeList;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void addAttribute(Attribute attribute) {
        if (this.attributeList == null) {
            this.attributeList = new ArrayList<>();
        }
        this.attributeList.add(attribute);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "Element{" +
                "uniqueName='" + uniqueName + '\'' +
                ", comment='" + comment + '\'' +
                ", attributeList=" + attributeList +
                '}';
    }
}
