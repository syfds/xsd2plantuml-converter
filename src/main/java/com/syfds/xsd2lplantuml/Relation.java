package com.syfds.xsd2lplantuml;

public class Relation {

    private String source;
    private String target;
    private RelationType type;

    public Relation(String source, String target, RelationType type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public RelationType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", type=" + type +
                '}';
    }
}
