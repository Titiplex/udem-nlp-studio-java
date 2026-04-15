package org.titiplex.backend.service;

import org.junit.jupiter.api.Test;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleDraftResultDto;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuleEditorServiceTest {

    private final RuleEditorService ruleEditorService = new RuleEditorService();

    @Test
    void validateShouldReportMissingRequiredFields() {
        RuleDraftResultDto result = ruleEditorService.validate(new RuleDetailDto(
                null,
                "",
                null,
                "",
                "",
                true,
                0,
                "",
                Map.of(),
                ""
        ));

        assertFalse(result.issues().isEmpty());
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("name")));
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("kind")));
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("subtype")));
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("scope")));
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("payload")));
    }

    @Test
    void parseYamlIntoDraftShouldExtractNameScopeAndPayload() {
        RuleDetailDto input = new RuleDetailDto(
                UUID.randomUUID(),
                "",
                RuleKind.ANNOTATION,
                "conllu",
                "",
                true,
                10,
                "desc",
                Map.of(),
                """
                        - name: Agreement rule
                          scope: token
                          match:
                            gloss:
                              in_lexicon: spanish_verbs
                          set:
                            upos: VERB
                        """
        );

        RuleDraftResultDto result = ruleEditorService.parseYamlIntoDraft(input);

        assertEquals("Agreement rule", result.rule().name());
        assertEquals("token", result.rule().scope());
        assertTrue(result.rule().payload().containsKey("match"));
        assertTrue(result.rule().payload().containsKey("set"));
    }

    @Test
    void parseYamlIntoDraftShouldReportInvalidYaml() {
        RuleDetailDto input = new RuleDetailDto(
                UUID.randomUUID(),
                "x",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                10,
                "",
                Map.of(),
                "not: [valid"
        );

        RuleDraftResultDto result = ruleEditorService.parseYamlIntoDraft(input);

        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("rawYaml")));
        assertTrue(result.issues().stream().anyMatch(i -> i.message().contains("YAML invalide")));
    }

    @Test
    void generateYamlFromDraftShouldSerializePayloadBackToYaml() {
        RuleDetailDto input = new RuleDetailDto(
                UUID.randomUUID(),
                "Agreement rule",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                10,
                "Rule description",
                Map.of(
                        "match", Map.of("gloss", Map.of("in_lexicon", "spanish_verbs")),
                        "set", Map.of("upos", "VERB")
                ),
                ""
        );

        RuleDraftResultDto result = ruleEditorService.generateYamlFromDraft(input);

        assertTrue(result.rule().rawYaml().contains("name: Agreement rule"));
        assertTrue(result.rule().rawYaml().contains("scope: token"));
        assertTrue(result.rule().rawYaml().contains("upos: VERB"));
    }

    @Test
    void toDomainShouldPreserveFields() {
        RuleDetailDto input = new RuleDetailDto(
                UUID.randomUUID(),
                "Agreement rule",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                25,
                "desc",
                Map.of("set", Map.of("upos", "VERB")),
                "- name: Agreement rule"
        );

        var domain = ruleEditorService.toDomain(input);

        assertEquals(input.id(), domain.id());
        assertEquals(input.name(), domain.name());
        assertEquals(input.kind(), domain.kind());
        assertEquals(input.priority(), domain.priority());
        assertEquals(input.rawYaml(), domain.rawYaml());
    }
}