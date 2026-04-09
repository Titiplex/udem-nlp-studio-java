package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleSummaryDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.RuleStore;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RuleService {

    private final ProjectContextService projectContextService;
    private final RuleStore ruleStore;

    public RuleService(ProjectContextService projectContextService,
                       RuleStore ruleStore) {
        this.projectContextService = projectContextService;
        this.ruleStore = ruleStore;
    }

    public List<RuleSummaryDto> listRules() {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        return ruleStore.listRules(project);
    }

    public RuleDetailDto getRule(UUID id) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        return ruleStore.getRule(project, id);
    }

    public RuleDetailDto saveRule(RuleDetailDto dto) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        return ruleStore.saveRule(project, dto, SaveOptions.standard(project.localMember().principalId()));
    }

    public RuleDetailDto createEmptyRule(String kind, String subtype) {
        return new RuleDetailDto(
                UUID.randomUUID(),
                "",
                RuleKind.valueOf(kind.toUpperCase()),
                subtype,
                "token",
                true,
                100,
                "",
                Map.of(),
                "",
                0L,
                "",
                Instant.now()
        );
    }
}