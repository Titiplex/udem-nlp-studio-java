package org.titiplex.rules.registry;

import java.util.List;

public record RuleBuilderSchema(
        String kind,
        String subtype,
        String label,
        String description,
        List<FieldDescriptor> fields
) {
}