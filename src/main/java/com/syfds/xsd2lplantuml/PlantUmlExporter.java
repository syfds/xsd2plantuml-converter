package com.syfds.xsd2lplantuml;

public class PlantUmlExporter {

    public String export(EntityRelationshipModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");

        addEntities(model, sb);
        addRelations(model, sb);

        sb.append("@enduml\n");

        return sb.toString();
    }

    private static void addRelations(EntityRelationshipModel model, StringBuilder sb) {
        model.getRelationships().forEach(relation -> {
            String source = relation.getSource();
            String target = relation.getTarget();

            RelationType type = relation.getType();
            sb.append(source).append(" ").append(type.getPlantumlNotation()).append(target);
            sb.append("\n");
        });
    }

    private static void addEntities(EntityRelationshipModel model, StringBuilder sb) {
        for (Entity entity : model.getEntities()) {
            entityStart(sb, entity);
            for (Attribute attribute : entity.getAttributeList()) {
                appendCommentIfExisting(sb, attribute);
                appenAttributeName(sb, attribute);
                sb.append("\n");
            }
            entityEnd(sb);
            if (entity.getComment() != null && !entity.getComment().isEmpty()) {
                sb.append("note top of ").append(entity.getUniqueName()).append("\n").append(entity.getComment()).append("\n").append("end note\n");
            }
        }
    }

    private static void entityEnd(StringBuilder sb) {
        sb.append("}\n");
    }

    private static void entityStart(StringBuilder sb, Entity entity) {
        sb.append("entity \"").append(entity.getUniqueName()).append("\" {\n");
    }

    private static void appenAttributeName(StringBuilder sb, Attribute attribute) {
        sb.append("  ").append(attribute.getName()).append(" : ").append(getType(attribute));
    }

    private static String getType(Attribute attribute) {
        String type = attribute.getType();
        if (attribute.getRelationType() == RelationType.ONE_TO_MANY) {
            type = type + "[]";
        }
        return type;
    }

    private static void appendCommentIfExisting(StringBuilder sb, Attribute attribute) {
        if (attribute.getComment() != null && !attribute.getComment().isEmpty()) {
            String[] commentLines = attribute.getComment().split("\n");
            for (String line : commentLines) {
                boolean shouldSkipEmptyLine = line.trim().isEmpty();
                if (shouldSkipEmptyLine) {
                    continue;
                }
                sb.append("  <color:grey>// ").append(line).append("</color>\n");
            }
        }
    }
}
