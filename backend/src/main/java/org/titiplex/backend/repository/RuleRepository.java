package org.titiplex.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.model.RuleEntity;

import java.util.List;
import java.util.UUID;

public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {
    List<RuleEntity> findByKindOrderByPriorityAscNameAsc(RuleKind kind);
}