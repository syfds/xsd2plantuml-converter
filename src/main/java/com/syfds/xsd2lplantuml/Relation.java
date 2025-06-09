package com.syfds.xsd2lplantuml;

import java.util.Objects;

public class Relation {

    private String source;
    private String target;
    private RelationType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(source, relation.source) && Objects.equals(target, relation.target) && type == relation.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type);
    }

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
