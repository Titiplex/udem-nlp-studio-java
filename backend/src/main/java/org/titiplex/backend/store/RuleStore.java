package org.titiplex.backend.store;

import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleSummaryDto;
import org.titiplex.backend.project.ProjectContext;

import java.util.List;
import java.util.UUID;

public interface RuleStore {
    List<RuleSummaryDto> listRules(ProjectContext project);

    RuleDetailDto getRule(ProjectContext project, UUID id);

    RuleDetailDto saveRule(ProjectContext project, RuleDetailDto dto, SaveOptions options);

    List<RuleDetailDto> listRulesByKind(ProjectContext project, RuleKind kind);
}