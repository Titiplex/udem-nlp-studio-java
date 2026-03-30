package org.titiplex.rules.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultRuleSchemaRegistry implements RuleSchemaRegistry {

    private final List<RuleBuilderSchema> schemas = List.of(
            correctionSplitSchema(),
            correctionRewriteSchema(),
            annotationConlluSchema()
    );

    @Override
    public List<RuleDescriptor> listRuleDescriptors() {
        return schemas.stream()
                .map(schema -> new RuleDescriptor(
                        schema.kind(),
                        schema.subtype(),
                        schema.label(),
                        schema.description()
                ))
                .toList();
    }

    @Override
    public List<RuleBuilderSchema> listBuilderSchemas() {
        return schemas;
    }

    @Override
    public Optional<RuleBuilderSchema> findBuilderSchema(String kind, String subtype) {
        return schemas.stream()
                .filter(schema -> schema.kind().equalsIgnoreCase(kind))
                .filter(schema -> schema.subtype().equalsIgnoreCase(subtype))
                .findFirst();
    }

    private static RuleBuilderSchema correctionSplitSchema() {
        return new RuleBuilderSchema(
                "correction",
                "split",
                "Split rule",
                "Split a token according to matching conditions.",
                List.of(
                        FieldDescriptor.simple("name", "Name", FieldType.TEXT, true, "Rule display name"),
                        FieldDescriptor.simple("scope", "Scope", FieldType.SELECT, true, "token / sequence / lexeme"),
                        new FieldDescriptor(
                                "match",
                                "Match",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Matching constraints",
                                "",
                                List.of(),
                                Map.of(),
                                List.of(
                                        FieldDescriptor.simple("token", "Token matcher", FieldType.TEXTAREA, false, "Token match expression"),
                                        FieldDescriptor.simple("gloss", "Gloss matcher", FieldType.TEXTAREA, false, "Gloss match expression")
                                )
                        ),
                        new FieldDescriptor(
                                "set",
                                "Set",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Correction operation",
                                "",
                                List.of(),
                                Map.of(),
                                List.of(
                                        FieldDescriptor.simple("operation", "Operation", FieldType.SELECT, true, "split"),
                                        FieldDescriptor.simple("position", "Position", FieldType.TEXT, false, "Split position expression")
                                )
                        )
                )
        );
    }

    private static RuleBuilderSchema correctionRewriteSchema() {
        return new RuleBuilderSchema(
                "correction",
                "rewrite",
                "Rewrite rule",
                "Rewrite text or gloss segments.",
                List.of(
                        FieldDescriptor.simple("name", "Name", FieldType.TEXT, true, "Rule display name"),
                        FieldDescriptor.simple("scope", "Scope", FieldType.SELECT, true, "token / sequence / lexeme"),
                        FieldDescriptor.simple("description", "Description", FieldType.TEXTAREA, false, "Optional note"),
                        new FieldDescriptor(
                                "set",
                                "Set",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Rewrite parameters",
                                "",
                                List.of(),
                                Map.of(),
                                List.of(
                                        FieldDescriptor.simple("operation", "Operation", FieldType.SELECT, true, "rewrite"),
                                        FieldDescriptor.simple("before", "Before", FieldType.STRING_LIST, false, "Values to replace"),
                                        FieldDescriptor.simple("after", "After", FieldType.STRING_LIST, false, "Replacement values")
                                )
                        )
                )
        );
    }

    private static RuleBuilderSchema annotationConlluSchema() {
        return new RuleBuilderSchema(
                "annotation",
                "conllu",
                "CoNLL-U annotation rule",
                "Annotate POS, features, or templates for CoNLL-U export.",
                List.of(
                        FieldDescriptor.simple("name", "Name", FieldType.TEXT, true, "Rule display name"),
                        FieldDescriptor.simple("scope", "Scope", FieldType.SELECT, true, "token / sequence / lexeme"),
                        FieldDescriptor.simple("upos", "UPOS", FieldType.TEXT, false, "Universal POS value"),
                        new FieldDescriptor(
                                "feats",
                                "Features",
                                FieldType.KEY_VALUE,
                                false,
                                false,
                                "Static feature mapping",
                                "",
                                List.of(),
                                Map.of(),
                                List.of()
                        ),
                        new FieldDescriptor(
                                "featsTemplate",
                                "Feature templates",
                                FieldType.KEY_VALUE,
                                false,
                                false,
                                "Dynamic template mapping",
                                "",
                                List.of(),
                                Map.of(),
                                List.of()
                        ),
                        new FieldDescriptor(
                                "extract",
                                "Extractors",
                                FieldType.OBJECT_LIST,
                                false,
                                true,
                                "Extractor declarations",
                                "",
                                List.of(),
                                Map.of(),
                                List.of(
                                        FieldDescriptor.simple("type", "Type", FieldType.TEXT, true, "scan_agreement etc."),
                                        FieldDescriptor.simple("extractor", "Extractor", FieldType.TEXT, true, "Extractor name"),
                                        FieldDescriptor.simple("into", "Into", FieldType.TEXT, false, "Target variable")
                                )
                        )
                )
        );
    }
}