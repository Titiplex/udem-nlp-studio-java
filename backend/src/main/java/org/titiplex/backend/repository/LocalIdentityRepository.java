package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.model.LocalIdentityEntity;

public interface LocalIdentityRepository extends JpaRepository<LocalIdentityEntity, Long> {
}