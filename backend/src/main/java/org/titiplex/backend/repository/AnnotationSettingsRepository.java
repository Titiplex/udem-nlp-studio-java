package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.AnnotationSettingsEntity;

public interface AnnotationSettingsRepository extends JpaRepository<AnnotationSettingsEntity, Long> {
}