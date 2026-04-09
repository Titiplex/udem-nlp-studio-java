package org.titiplex.backend.dto;

import java.time.Instant;

public record AnnotationSettingsDto(
        String posDefinitionsYaml,
        String featDefinitionsYaml,
        String lexiconsYaml,
        String extractorsYaml,
        String glossMapYaml,
        String baseYamlPreview,
        String effectiveYamlPreview,
        Long version,
        String updatedBy,
        Instant updatedAt
) {
}