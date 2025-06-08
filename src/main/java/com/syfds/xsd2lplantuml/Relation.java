package com.syfds.xsd2lplantuml;

public class Relation {

    private Element source;
    private Element target;
    private RelationType type;

    public Relation(Element source, Element target, RelationType type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public Element getSource() {
        return source;
    }

    public Element getTarget() {
        return target;
    }

    public RelationType getType() {
        return type;
    }
}
