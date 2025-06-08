package com.syfds.xsd2lplantuml;

import java.util.List;

public class EntityRelationshipModel {

    private List<Element> elements;
    private List<Relation> relationships;



    public EntityRelationshipModel(List<Element> elements, List<Relation> relationships) {
        this.elements = elements;
        this.relationships = relationships;
    }

    public List<Relation> getRelationships() {
        return relationships;
    }

    public List<Element> getElements() {
            return elements;
    }
}
