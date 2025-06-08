package com.syfds.xsd2lplantuml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        if (args.length != 2) {
            System.err.println("Usage: java -jar xsd2plantuml.jar <input-xsd-filepath> <output-filepath>");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try {
            XsdReader xsdReader = new XsdReader();
            EntityRelationshipModel model = xsdReader.readXsd(inputFilePath);

            String outputContent = new PlantUmlExporter().export(model);
            Files.writeString(Path.of(outputFilePath), outputContent);

            System.out.println("Output successfully written to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing XSD file: " + e.getMessage());
        }    }
}
