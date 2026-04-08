package org.titiplex.backend.dto;

public record RuleDescriptorDto(
        String kind,
        String subtype,
        String label,
        String description
) {
}