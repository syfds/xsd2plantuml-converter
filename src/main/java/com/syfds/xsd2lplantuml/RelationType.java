package com.syfds.xsd2lplantuml;

public enum RelationType {
    TYPE_OF("\"*\"-- "),
    ONE_TO_ONE("\"1\"--\"1\""),
    ONE_TO_MANY("\"1\"--\"*\""),
    MANY_TO_ONE("\"*\"--\"1\""),
    MANY_TO_MANY("\"*\"--\"*\"");

    private final String plantumlNotation;

    RelationType(String plantumlNotation) {
        this.plantumlNotation = plantumlNotation;
    }

    public String getPlantumlNotation() {
        return plantumlNotation;
    }
}
