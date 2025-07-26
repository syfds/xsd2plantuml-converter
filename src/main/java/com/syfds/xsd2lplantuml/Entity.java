package com.syfds.xsd2lplantuml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entity {

    private String uniqueName;
    private String type;
    private String extensionType;
    private String comment;
    private List<Attribute> attributeList = new ArrayList<>();

    public Entity(String uniqueName) {
        this.uniqueName = uniqueName;
    }


    public Attribute findAttributeByName(String name ) {
        return attributeList.stream()
                .filter(attribute -> attribute.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Attribute with name " + name + " not found in entity " + uniqueName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(uniqueName, entity.uniqueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueName);
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

    public String getType() {
        if (type != null) {
            return type;
        } else if (extensionType != null) {
            return extensionType;
        }
        return null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExtensionType(String extensionType) {
        this.extensionType = extensionType;
    }

    public RelationType getRelationType(){
        if (extensionType != null) {
            return RelationType.EXTENSION;
        } else {
            return RelationType.TYPE_OF;
        }
    }
}
