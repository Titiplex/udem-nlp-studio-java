package org.titiplex.backend.store;

import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.project.ProjectContext;

public interface AnnotationSettingsStore {
    AnnotationSettingsDto getSettings(ProjectContext project);

    AnnotationSettingsDto saveSettings(ProjectContext project, AnnotationSettingsDto dto, SaveOptions options);
}