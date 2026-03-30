package org.titiplex.backend.dto;

import java.util.List;

public record RuleBuilderSchemaDto(
        String kind,
        String subtype,
        String label,
        String description,
        List<FieldDescriptorDto> fields
) {
}