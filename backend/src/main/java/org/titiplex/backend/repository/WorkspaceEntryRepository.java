package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.WorkspaceEntryEntity;

import java.util.List;
import java.util.UUID;

public interface WorkspaceEntryRepository extends JpaRepository<WorkspaceEntryEntity, UUID> {
    List<WorkspaceEntryEntity> findAllByOrderByDocumentOrderAscIdAsc();
}