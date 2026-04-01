package org.titiplex.backend.dto;

public record ValidationIssueDto(
        String path,
        String level,
        String message
) {
}