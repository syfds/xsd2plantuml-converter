package com.syfds.xsd2lplantuml;

public class PlantUmlExporter {

    public String export(EntityRelationshipModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");

        addTypes(model, sb);
        addRelations(model, sb);

        sb.append("@enduml\n");

        return sb.toString();
    }

    private void addRelations(EntityRelationshipModel model, StringBuilder sb) {
        model.getRelationships().forEach(relation -> {
            String source = relation.getSource();
            String target = relation.getTarget();

            RelationType type = relation.getType();
            sb.append(source).append(" ").append(type.getPlantumlNotation()).append(target);
            sb.append("\n");
        });
    }

    private static void addTypes(EntityRelationshipModel model, StringBuilder sb) {
        for (Entity entity : model.getEntities()) {
            sb.append("entity \"").append(entity.getUniqueName()).append("\" {\n");
            for (Attribute attribute : entity.getAttributeList()) {
                appendCommentIfExisting(sb, attribute);
                sb.append("  ").append(attribute.getName()).append(" : ").append(attribute.getType());
                sb.append("\n");
            }
            sb.append("}\n");
            if (entity.getComment() != null && !entity.getComment().isEmpty()) {
                sb.append("note top of ").append(entity.getUniqueName()).append("\n").append(entity.getComment()).append("\n").append("end note\n");
            }
        }
    }

    private static void appendCommentIfExisting(StringBuilder sb, Attribute attribute) {
        if (attribute.getComment() != null && !attribute.getComment().isEmpty()) {
            String[] commentLines = attribute.getComment().split("\n");
            for (String line : commentLines) {
                boolean shouldSkipEmptyLine = line.trim().isEmpty();
                if(shouldSkipEmptyLine) {
                    continue;
                }
                sb.append("  <color:grey>// ").append(line).append("</color>\n");
            }
        }
    }
}
