package org.titiplex.backend.dto;

public record TextExportDto(
        String fileName,
        String content
) {
}