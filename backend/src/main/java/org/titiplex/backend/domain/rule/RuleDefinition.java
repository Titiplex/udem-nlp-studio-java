package org.titiplex.backend.domain.rule;

import java.util.Map;
import java.util.UUID;

public record RuleDefinition(
        UUID id,
        String name,
        RuleKind kind,
        String subtype,
        String scope,
        boolean enabled,
        int priority,
        String description,
        Map<String, Object> payload,
        String rawYaml
) {
}