package com.syfds.xsd2lplantuml;


import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class XsdReaderTest {

    @Test
    public void test() throws SAXException {
        EntityRelationshipModel model = new XsdReader().readXsd(this.getClass().getResource("/person.xsd").getPath());
        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }
    @Test
    public void test2() throws SAXException {
        EntityRelationshipModel model = new XsdReader().readXsd(this.getClass().getResource("/animals/main.xsd").getPath());
        String plantumlAsString = new PlantUmlExporter().export(model);
        System.out.println(plantumlAsString);
    }

}
