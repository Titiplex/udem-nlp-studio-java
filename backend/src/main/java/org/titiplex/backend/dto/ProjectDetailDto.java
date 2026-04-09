package org.titiplex.backend.dto;

import java.util.List;
import java.util.UUID;

public record ProjectDetailDto(
        UUID projectId,
        String name,
        String version,
        boolean active,
        List<ProjectMemberDto> members,
        List<ProjectSourceDto> sources
) {
}