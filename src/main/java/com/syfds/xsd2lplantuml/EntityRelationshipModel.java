package com.syfds.xsd2lplantuml;

import java.util.List;
import java.util.Set;

public class EntityRelationshipModel {

    private Set<Entity> entities;
    private Set<Relation> relationships;



    public EntityRelationshipModel(Set<Entity> entities, Set<Relation> relationships) {
        this.entities = entities;
        this.relationships = relationships;
    }

    public Set<Relation> getRelationships() {
        return relationships;
    }

    public Set<Entity> getEntities() {
            return entities;
    }
}
