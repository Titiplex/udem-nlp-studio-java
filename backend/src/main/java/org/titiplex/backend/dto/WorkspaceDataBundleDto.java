package org.titiplex.backend.dto;

import java.util.List;

public record WorkspaceDataBundleDto(
        List<EntryDetailDto> entries,
        List<RuleDetailDto> rules
) {
}