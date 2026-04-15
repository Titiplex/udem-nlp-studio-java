package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleSummaryDto;
import org.titiplex.backend.repository.RuleRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class RuleServiceTest {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleRepository ruleRepository;

    @BeforeEach
    void setUp() {
        ruleRepository.deleteAll();
    }

    @Test
    void saveRuleThenListAndReloadShouldWork() {
        RuleDetailDto saved = ruleService.saveRule(new RuleDetailDto(
                null,
                "Verb agreement",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                20,
                "Extract agreement features.",
                Map.of(
                        "match", Map.of("gloss", Map.of("in_lexicon", "spanish_verbs")),
                        "set", Map.of("upos", "VERB")
                ),
                """
                        - name: Verb agreement
                          scope: token
                          match:
                            gloss:
                              in_lexicon: spanish_verbs
                          set:
                            upos: VERB
                        """
        ));

        assertNotNull(saved.id());
        assertEquals("Verb agreement", saved.name());
        assertEquals(RuleKind.ANNOTATION, saved.kind());

        List<RuleSummaryDto> summaries = ruleService.listRules();
        assertEquals(1, summaries.size());
        assertEquals("Verb agreement", summaries.getFirst().name());
        assertEquals("conllu", summaries.getFirst().subtype());

        RuleDetailDto loaded = ruleService.getRule(saved.id());
        assertEquals(saved.id(), loaded.id());
        assertEquals("Verb agreement", loaded.name());
        assertTrue(loaded.enabled());
        assertEquals(20, loaded.priority());
        assertTrue(loaded.rawYaml().contains("upos: VERB"));
    }

    @Test
    void createEmptyRuleShouldProvideUsableDefaults() {
        RuleDetailDto dto = ruleService.createEmptyRule("annotation", "conllu");

        assertNotNull(dto.id());
        assertEquals(RuleKind.ANNOTATION, dto.kind());
        assertEquals("conllu", dto.subtype());
        assertEquals("token", dto.scope());
        assertTrue(dto.enabled());
        assertEquals(100, dto.priority());
        assertTrue(dto.payload().isEmpty());
    }

    @Test
    void getRuleShouldFailForUnknownId() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ruleService.getRule(java.util.UUID.randomUUID())
        );

        assertTrue(ex.getMessage().contains("Rule not found"));
    }

    @Test
    void seedDemoRulesIfEmptyShouldOnlySeedOnce() {
        ruleService.seedDemoRulesIfEmpty();
        long firstCount = ruleRepository.count();

        ruleService.seedDemoRulesIfEmpty();
        long secondCount = ruleRepository.count();

        assertEquals(2, firstCount);
        assertEquals(firstCount, secondCount);
    }
}