package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.ProjectRegistryEntity;

import java.util.List;
import java.util.UUID;

public interface ProjectRegistryRepository extends JpaRepository<ProjectRegistryEntity, UUID> {
    List<ProjectRegistryEntity> findAllByOrderByLastOpenedAtDescNameAsc();
}