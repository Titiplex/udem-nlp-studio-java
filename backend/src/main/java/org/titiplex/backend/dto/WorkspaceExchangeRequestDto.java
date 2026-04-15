package org.titiplex.backend.dto;

import java.util.List;

public record WorkspaceExchangeRequestDto(
        String format,
        boolean preferCorrected,
        boolean correctedOnly,
        List<String> ruleKinds,
        boolean onlyEnabledRules,
        boolean includeAnnotationSettings
) {
}