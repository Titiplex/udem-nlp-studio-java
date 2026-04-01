package org.titiplex.backend.dto;

import java.util.List;

public record RuleDraftResultDto(
        RuleDetailDto rule,
        List<ValidationIssueDto> issues
) {
}