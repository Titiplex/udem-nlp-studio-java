package org.titiplex.rules.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultRuleSchemaRegistry implements RuleSchemaRegistry {

    private static final List<String> SCOPES = List.of("token", "sequence", "lexeme");

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
                "Split a token according to matching conditions and a split position.",
                List.of(
                        textField("name", "Name", true, "Rule display name"),
                        scopeField(),
                        descriptionField(),

                        new FieldDescriptor(
                                "match",
                                "Match",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Matching constraints",
                                "Conditions that decide whether the rule applies.",
                                List.of(),
                                Map.of(
                                        "token", "",
                                        "gloss", ""
                                ),
                                List.of(
                                        textAreaField("token", "Token matcher", false,
                                                "Text or mini-expression for token matching"),
                                        textAreaField("gloss", "Gloss matcher", false,
                                                "Text or mini-expression for gloss matching")
                                )
                        ),

                        new FieldDescriptor(
                                "set",
                                "Set",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Split operation parameters",
                                "Defines the correction action for the rule.",
                                List.of(),
                                Map.of(
                                        "operation", "split",
                                        "position", "end"
                                ),
                                List.of(
                                        selectField(
                                                "operation",
                                                "Operation",
                                                true,
                                                "Correction action to apply",
                                                List.of("split")
                                        ),
                                        textField(
                                                "position",
                                                "Position",
                                                false,
                                                "Split position: end, start, or a custom expression"
                                        )
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
                "Rewrite token or gloss segments using ordered replacement lists.",
                List.of(
                        textField("name", "Name", true, "Rule display name"),
                        scopeField(),
                        descriptionField(),

                        new FieldDescriptor(
                                "match",
                                "Match",
                                FieldType.OBJECT,
                                false,
                                false,
                                "Optional matching constraints",
                                "Leave empty to apply broadly, or add token / gloss constraints.",
                                List.of(),
                                Map.of(
                                        "token", "",
                                        "gloss", ""
                                ),
                                List.of(
                                        textAreaField("token", "Token matcher", false,
                                                "Optional token matcher"),
                                        textAreaField("gloss", "Gloss matcher", false,
                                                "Optional gloss matcher")
                                )
                        ),

                        new FieldDescriptor(
                                "set",
                                "Set",
                                FieldType.OBJECT,
                                true,
                                false,
                                "Rewrite parameters",
                                "Defines the replacement action and its ordered lists.",
                                List.of(),
                                Map.of(
                                        "operation", "rewrite",
                                        "before", List.of(),
                                        "after", List.of()
                                ),
                                List.of(
                                        selectField(
                                                "operation",
                                                "Operation",
                                                true,
                                                "Correction action to apply",
                                                List.of("rewrite")
                                        ),
                                        new FieldDescriptor(
                                                "before",
                                                "Before",
                                                FieldType.STRING_LIST,
                                                false,
                                                true,
                                                "Values to replace",
                                                "Ordered list of values to match before replacement.",
                                                List.of(),
                                                Map.of(),
                                                List.of()
                                        ),
                                        new FieldDescriptor(
                                                "after",
                                                "After",
                                                FieldType.STRING_LIST,
                                                false,
                                                true,
                                                "Replacement values",
                                                "Ordered list of replacement values.",
                                                List.of(),
                                                Map.of(),
                                                List.of()
                                        )
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
                "Annotate POS, features, templates, and extractor-based values for CoNLL-U export.",
                List.of(
                        textField("name", "Name", true, "Rule display name"),
                        scopeField(),
                        descriptionField(),

                        new FieldDescriptor(
                                "match",
                                "Match",
                                FieldType.OBJECT,
                                false,
                                false,
                                "Optional matching constraints",
                                "Conditions for selecting the token, sequence, or lexeme to annotate.",
                                List.of(),
                                Map.of(
                                        "token", "",
                                        "gloss", ""
                                ),
                                List.of(
                                        textAreaField("token", "Token matcher", false,
                                                "Optional token matcher"),
                                        textAreaField("gloss", "Gloss matcher", false,
                                                "Optional gloss matcher or JSON object")
                                )
                        ),

                        textField("upos", "UPOS", false,
                                "Universal POS tag to assign, e.g. VERB, NOUN, ADJ"),

                        new FieldDescriptor(
                                "feats",
                                "Features",
                                FieldType.KEY_VALUE,
                                false,
                                false,
                                "Static features",
                                "Static CoNLL-U features, e.g. Number=Sing.",
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
                                "Dynamic feature templates",
                                "Dynamic values such as {agreement_verbs.A.person}.",
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
                                "Ordered list of extractor calls to enrich the context before templates are resolved.",
                                List.of(),
                                Map.of(),
                                List.of(
                                        textField("type", "Type", true,
                                                "Extractor action type, e.g. scan_agreement"),
                                        textField("extractor", "Extractor", true,
                                                "Name of the registered extractor"),
                                        textField("into", "Into", false,
                                                "Optional target variable / alias in the context")
                                )
                        )
                )
        );
    }

    private static FieldDescriptor textField(
            String key,
            String label,
            boolean required,
            String helpText
    ) {
        return new FieldDescriptor(
                key,
                label,
                FieldType.TEXT,
                required,
                false,
                "",
                helpText,
                List.of(),
                Map.of(),
                List.of()
        );
    }

    private static FieldDescriptor textAreaField(
            String key,
            String label,
            boolean required,
            String helpText
    ) {
        return new FieldDescriptor(
                key,
                label,
                FieldType.TEXTAREA,
                required,
                false,
                "",
                helpText,
                List.of(),
                Map.of(),
                List.of()
        );
    }

    private static FieldDescriptor selectField(
            String key,
            String label,
            boolean required,
            String helpText,
            List<String> enumValues
    ) {
        return new FieldDescriptor(
                key,
                label,
                FieldType.SELECT,
                required,
                false,
                "",
                helpText,
                enumValues,
                Map.of(),
                List.of()
        );
    }

    private static FieldDescriptor scopeField() {
        return new FieldDescriptor(
                "scope",
                "Scope",
                FieldType.SELECT,
                true,
                false,
                "",
                "Granularity where the rule applies.",
                SCOPES,
                Map.of("value", "token"),
                List.of()
        );
    }

    private static FieldDescriptor descriptionField() {
        return new FieldDescriptor(
                "description",
                "Description",
                FieldType.TEXTAREA,
                false,
                false,
                "",
                "Optional note explaining the purpose of the rule.",
                List.of(),
                Map.of(),
                List.of()
        );
    }
}