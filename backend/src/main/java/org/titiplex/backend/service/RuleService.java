package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleDefinition;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleSummaryDto;
import org.titiplex.backend.mapper.RuleMapper;
import org.titiplex.backend.model.RuleEntity;
import org.titiplex.backend.repository.RuleRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    public RuleService(RuleRepository ruleRepository, RuleMapper ruleMapper) {
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
    }

    public List<RuleSummaryDto> listRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toDomain)
                .map(this::toSummaryDto)
                .toList();
    }

    public RuleDetailDto getRule(UUID id) {
        RuleDefinition definition = ruleRepository.findById(id)
                .map(ruleMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
        return toDetailDto(definition);
    }

    public RuleDetailDto saveRule(RuleDetailDto dto) {
        RuleDefinition definition = new RuleDefinition(
                dto.id() != null ? dto.id() : UUID.randomUUID(),
                dto.name(),
                dto.kind(),
                dto.subtype(),
                dto.scope(),
                dto.enabled(),
                dto.priority(),
                dto.description(),
                dto.payload(),
                dto.rawYaml()
        );

        RuleEntity saved = ruleRepository.save(ruleMapper.toEntity(definition));
        return toDetailDto(ruleMapper.toDomain(saved));
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
                ""
        );
    }

    public void seedDemoRulesIfEmpty() {
        if (ruleRepository.count() > 0) {
            return;
        }

        saveRule(new RuleDetailDto(
                UUID.randomUUID(),
                "Split directional suffix",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                10,
                "Split final directional morphemes when conditions are met.",
                Map.of(
                        "match", Map.of("has_segment", "DIR"),
                        "set", Map.of("type", "split", "position", "end")
                ),
                """
                        - name: Split directional suffix
                          scope: token
                          match:
                            has_segment: DIR
                          set:
                            type: split
                            position: end
                        """
        ));

        saveRule(new RuleDetailDto(
                UUID.randomUUID(),
                "Scan agreement on verb tokens",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                20,
                "Extract verbal agreement and assign CoNLL-U features.",
                Map.of(
                        "match", Map.of("gloss", Map.of("in_lexicon", "spanish_verbs")),
                        "set", Map.of(
                                "upos", "VERB",
                                "extract", List.of(Map.of(
                                        "type", "scan_agreement",
                                        "extractor", "agreement_verbs",
                                        "into", "agreement_verbs"
                                )),
                                "feats_template", Map.of(
                                        "Pers[subj]", "{agreement_verbs.A.person}",
                                        "Number[subj]", "{agreement_verbs.A.number}",
                                        "Pers[obj]", "{agreement_verbs.B.person}",
                                        "Number[obj]", "{agreement_verbs.B.number}"
                                )
                        )
                ),
                """
                        - name: Scan agreement on verb tokens
                          scope: token
                          match:
                            gloss:
                              in_lexicon: spanish_verbs
                          set:
                            upos: VERB
                            extract:
                              - type: scan_agreement
                                extractor: agreement_verbs
                                into: agreement_verbs
                            feats_template:
                              Pers[subj]: "{agreement_verbs.A.person}"
                              Number[subj]: "{agreement_verbs.A.number}"
                              Pers[obj]: "{agreement_verbs.B.person}"
                              Number[obj]: "{agreement_verbs.B.number}"
                        """
        ));
    }

    private RuleSummaryDto toSummaryDto(RuleDefinition definition) {
        return new RuleSummaryDto(
                definition.id(),
                definition.name(),
                definition.kind(),
                definition.subtype(),
                definition.scope(),
                definition.enabled(),
                definition.priority()
        );
    }

    private RuleDetailDto toDetailDto(RuleDefinition definition) {
        return new RuleDetailDto(
                definition.id(),
                definition.name(),
                definition.kind(),
                definition.subtype(),
                definition.scope(),
                definition.enabled(),
                definition.priority(),
                definition.description(),
                definition.payload(),
                definition.rawYaml()
        );
    }
}