package com.syfds.xsd2lplantuml;

public class Relation {

    private Entity source;
    private Entity target;
    private RelationType type;

    public Relation(Entity source, Entity target, RelationType type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public Entity getSource() {
        return source;
    }

    public Entity getTarget() {
        return target;
    }

    public RelationType getType() {
        return type;
    }
}
