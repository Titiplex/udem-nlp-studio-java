package org.titiplex.backend.dto;

import org.titiplex.backend.domain.rule.RuleKind;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RuleDetailDto(
        UUID id,
        String name,
        RuleKind kind,
        String subtype,
        String scope,
        boolean enabled,
        int priority,
        String description,
        Map<String, Object> payload,
        String rawYaml,
        Long version,
        String updatedBy,
        Instant updatedAt
) {
}