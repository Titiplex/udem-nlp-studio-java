package org.titiplex.backend.dto;

import java.util.List;
import java.util.Map;

public record FieldDescriptorDto(
        String key,
        String label,
        String type,
        boolean required,
        boolean repeatable,
        String placeholder,
        String helpText,
        List<String> enumValues,
        Map<String, Object> defaultValue,
        List<FieldDescriptorDto> nestedFields
) {
}