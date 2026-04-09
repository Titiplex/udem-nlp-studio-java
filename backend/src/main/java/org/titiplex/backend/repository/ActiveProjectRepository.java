package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.ActiveProjectEntity;

public interface ActiveProjectRepository extends JpaRepository<ActiveProjectEntity, Long> {
}