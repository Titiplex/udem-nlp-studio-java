package org.titiplex.backend.store;

import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.dto.EntryDetailDto;
import org.titiplex.backend.dto.EntrySummaryDto;
import org.titiplex.backend.dto.WorkspaceImportRequestDto;
import org.titiplex.backend.dto.WorkspaceImportResultDto;
import org.titiplex.backend.project.ProjectContext;

import java.util.List;
import java.util.UUID;

public interface WorkspaceEntryStore {
    List<EntrySummaryDto> listEntries(ProjectContext project);

    EntryDetailDto getEntry(ProjectContext project, UUID id);

    EntryDetailDto saveEntry(ProjectContext project, EntryDetailDto dto, SaveOptions options);

    WorkspaceImportResultDto importEntries(ProjectContext project, WorkspaceImportRequestDto request);
}