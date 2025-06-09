package com.syfds.xsd2lplantuml;

import java.util.List;

public class EntityRelationshipModel {

    private List<Entity> entities;
    private List<Relation> relationships;



    public EntityRelationshipModel(List<Entity> entities, List<Relation> relationships) {
        this.entities = entities;
        this.relationships = relationships;
    }

    public List<Relation> getRelationships() {
        return relationships;
    }

    public List<Entity> getEntities() {
            return entities;
    }
}
