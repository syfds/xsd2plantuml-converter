# XSD to PlantUML Converter

## Overview
This project is a Java application that converts XSD (XML Schema Definition) files into PlantUML diagrams. It reads an XSD file, processes its structure, and generates a visual representation of the entity-relationship model in PlantUML format.

## Features
- Parse XSD files to extract entity-relationship models.
- Export the model to PlantUML format for visualization.
- Command-line interface for easy usage.

## Requirements
- Java 17 or higher
- Maven 3.6 or higher

## Installation and Usage
1. Clone the repository:
   ```bash
   git clone https://github.com/syfds/xsd2plantuml.git
   cd xsd2plantuml
   mvn clean package
   java -jar target/xsd2plantuml-1.0-SNAPSHOT.jar schema.xsd output.puml
    ```

## Build the project using Maven:  
The executable JAR file will be located in the target directory:  
`target/xsd2plantuml-jar-with-dependencies.jar`
