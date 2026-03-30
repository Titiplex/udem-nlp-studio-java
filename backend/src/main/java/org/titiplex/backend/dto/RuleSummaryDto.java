package org.titiplex.backend.dto;

import org.titiplex.backend.domain.rule.RuleKind;

import java.util.UUID;

public record RuleSummaryDto(
        UUID id,
        String name,
        RuleKind kind,
        String subtype,
        String scope,
        boolean enabled,
        int priority
) {
}