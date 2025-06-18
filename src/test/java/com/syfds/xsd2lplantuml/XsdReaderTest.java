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
        assertThat(model.getRelationships().iterator().next().getType()).isEqualTo(RelationType.TYPE_OF);
        model.getEntities().forEach(elem -> {
            assertThat(elem.getUniqueName()).containsAnyOf("person", "personType");
        });
    }

    @Test
    public void oneElementAndOneInnerType() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_element_and_inner_type.xsd"));
        assertThat(model.getEntities()).hasSize(1);

        assertThat(model.getRelationships()).isEmpty();
        assertThat(model.getEntities().iterator().next().getUniqueName()).isEqualTo("person");
    }

    @Test
    public void multipleElementsAndRelations() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/person.xsd"));
        assertThat(model.getEntities()).hasSize(5);
        assertThat(model.getRelationships()).hasSize(5);
    }

    @Test
    public void test() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/person.xsd"));
        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }
    @Test
    public void testTransitiveTypes() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_element_and_transitive_types.xsd"));
        assertThat(model.getEntities()).hasSize(5);

        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }
    @Test
    public void testTransitiveTypesSimpleContent() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_element_and_transitive_types_simple_content.xsd"));
        assertThat(model.getEntities()).hasSize(5);

        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }
    @Test
    public void testSimpleTypeAndEnum() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/only_person_simple_type_with_enum.xsd"));
        assertThat(model.getEntities()).hasSize(5);

        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }
    @Test
    public void testMultipleImportsAndAnnotation() {
        EntityRelationshipModel model = new XsdReader().mapToModel(getPath("/animals/main.xsd"));

        assertThat(model.getEntities()).hasSize(3);
        assertThat(model.getRelationships()).hasSize(2);

        Entity zoo = model.findEntityByName("Zoo");
        assertThat(zoo.getComment()).isEqualTo("Here is an example for an annotation");

        Entity bird = model.findEntityByName("Bird");

        assertThat(bird.findAttributeByName("Species").getComment())
                .containsAnyOf("Should be an enum!", "A multiline documentation tag.");


        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }

    private String getPath(String localPath) {
        return this.getClass().getResource(localPath).getPath();
    }
}
