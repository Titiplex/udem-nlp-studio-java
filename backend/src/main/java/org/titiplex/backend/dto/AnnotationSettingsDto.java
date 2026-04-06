package org.titiplex.backend.dto;

public record AnnotationSettingsDto(
        String posDefinitionsYaml,
        String featDefinitionsYaml,
        String lexiconsYaml,
        String extractorsYaml,
        String glossMapYaml,
        String baseYamlPreview,
        String effectiveYamlPreview
) {
}