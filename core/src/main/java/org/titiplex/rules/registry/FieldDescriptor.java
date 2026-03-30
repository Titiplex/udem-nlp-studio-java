package org.titiplex.rules.registry;

import java.util.List;
import java.util.Map;

public record FieldDescriptor(
        String key,
        String label,
        FieldType type,
        boolean required,
        boolean repeatable,
        String placeholder,
        String helpText,
        List<String> enumValues,
        Map<String, Object> defaultValue,
        List<FieldDescriptor> nestedFields
) {
    public static FieldDescriptor simple(
            String key,
            String label,
            FieldType type,
            boolean required,
            String helpText
    ) {
        return new FieldDescriptor(
                key,
                label,
                type,
                required,
                false,
                "",
                helpText,
                List.of(),
                Map.of(),
                List.of()
        );
    }
}