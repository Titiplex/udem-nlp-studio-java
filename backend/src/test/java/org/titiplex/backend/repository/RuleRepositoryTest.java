package org.titiplex.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.model.RuleEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class RuleRepositoryTest {

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    void findByKindOrderByPriorityAscNameAscShouldSortCorrectly() {
        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Zulu annotation",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                30,
                "desc",
                "{}",
                "- name: Zulu annotation"
        ));

        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Alpha annotation",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                30,
                "desc",
                "{}",
                "- name: Alpha annotation"
        ));

        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Correction rule",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                10,
                "desc",
                "{}",
                "- name: Correction rule"
        ));

        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Low priority annotation",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                5,
                "desc",
                "{}",
                "- name: Low priority annotation"
        ));

        List<RuleEntity> rules = ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.ANNOTATION);

        assertEquals(3, rules.size());
        assertEquals("Low priority annotation", rules.get(0).getName());
        assertEquals("Alpha annotation", rules.get(1).getName());
        assertEquals("Zulu annotation", rules.get(2).getName());
    }

    @Test
    void findByKindOrderByPriorityAscNameAscShouldReturnOnlyRequestedKind() {
        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Annotation A",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                1,
                "",
                "{}",
                "- name: Annotation A"
        ));

        ruleRepository.save(new RuleEntity(
                UUID.randomUUID(),
                "Correction B",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                1,
                "",
                "{}",
                "- name: Correction B"
        ));

        List<RuleEntity> annotationRules = ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.ANNOTATION);
        List<RuleEntity> correctionRules = ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.CORRECTION);

        assertEquals(1, annotationRules.size());
        assertEquals("Annotation A", annotationRules.getFirst().getName());

        assertEquals(1, correctionRules.size());
        assertEquals("Correction B", correctionRules.getFirst().getName());
    }
}