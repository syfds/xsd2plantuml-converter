package com.syfds.xsd2lplantuml;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class XsdReaderTest {

    @Test
    public void oneElementAndOneType() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_element_and_type.xsd"));
        assertThat(model.getEntities()).hasSize(2);

        assertThat(model.getRelationships()).hasSize(1);
        assertThat(model.getRelationships().get(0).getType()).isEqualTo(RelationType.TYPE_OF);
        assertThat(model.getEntities().get(0).getUniqueName()).containsAnyOf("person", "personType");
        assertThat(model.getEntities().get(1).getUniqueName()).containsAnyOf("person", "personType");
    }

    @Test
    public void oneElementAndOneInnerType() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_element_and_inner_type.xsd"));
        assertThat(model.getEntities()).hasSize(1);

        assertThat(model.getRelationships()).isEmpty();
        assertThat(model.getEntities().get(0).getUniqueName()).isEqualTo("person");
    }

    @Test
    public void multipleElementsAndRelations() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/person.xsd"));
        assertThat(model.getEntities()).hasSize(4);

        assertThat(model.getRelationships()).hasSize(4);
    }

    @Test
    public void test() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/person.xsd"));
        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }

    @Test
    public void test2() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/animals/main.xsd"));
        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }

    private String getPath(String localPath) {
        return this.getClass().getResource(localPath).getPath();
    }
}
