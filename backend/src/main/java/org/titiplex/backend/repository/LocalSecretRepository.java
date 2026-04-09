package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.LocalSecretEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalSecretRepository extends JpaRepository<LocalSecretEntity, UUID> {
    Optional<LocalSecretEntity> findByProjectIdAndSecretRef(UUID projectId, String secretRef);

    List<LocalSecretEntity> findAllByProjectId(UUID projectId);
}