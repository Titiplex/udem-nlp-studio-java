package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.repository.AnnotationSettingsRepository;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class WorkspaceEntryServiceTest {

    @Autowired
    private WorkspaceEntryService workspaceEntryService;

    @Autowired
    private WorkspaceEntryRepository workspaceEntryRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @BeforeEach
    void setUp() {
        workspaceEntryRepository.deleteAll();
        ruleRepository.deleteAll();
        annotationSettingsRepository.deleteAll();
    }

    @Test
    void saveAndListEntriesShouldWork() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Field note A",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment A",
                "",
                "",
                "",
                false,
                ""
        ));

        assertNotNull(saved.id());
        assertEquals("Field note A", saved.contextText());
        assertEquals("Ix naq", saved.surfaceText());
        assertEquals("Comment A", saved.comments());
        assertEquals(1, workspaceEntryService.listEntries().size());
        assertEquals("Ix-naq", workspaceEntryService.listEntries().getFirst().rawChujText());
    }

    @Test
    void getEntryShouldReturnStructuredFields() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Context 1",
                "Surface 1",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment 1",
                "",
                "",
                "",
                false,
                ""
        ));

        EntryDetailDto loaded = workspaceEntryService.getEntry(saved.id());

        assertEquals("Context 1", loaded.contextText());
        assertEquals("Surface 1", loaded.surfaceText());
        assertEquals("Comment 1", loaded.comments());
        assertEquals("Ix-naq", loaded.rawChujText());
    }

    @Test
    void runCorrectionWithoutRulesShouldStillPopulatePreview() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Field note A",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment A",
                "",
                "",
                "",
                false,
                ""
        ));

        EntryDetailDto corrected = workspaceEntryService.runCorrection(
                new CorrectionRunRequestDto(saved.id(), false)
        );

        assertEquals("Ix-naq", corrected.correctedChujText());
        assertEquals("A1-ganar", corrected.correctedGlossText());
        assertFalse(corrected.conlluPreview().isBlank());
    }

    @Test
    void approvedEntryShouldNotBeForcedByDefault() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Field note A",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Manual comment",
                "MANUAL",
                "MANUAL-GLOSS",
                "MANUAL-TRAD",
                true,
                "manual-preview"
        ));

        EntryDetailDto result = workspaceEntryService.runCorrection(
                new CorrectionRunRequestDto(saved.id(), false)
        );

        assertEquals("MANUAL", result.correctedChujText());
        assertEquals("Field note A", result.contextText());
        assertEquals("Ix naq", result.surfaceText());
        assertEquals("Manual comment", result.comments());
        assertFalse(result.conlluPreview().isBlank());
    }

    @Test
    void importEntriesShouldCreateWorkspaceBlocks() {
        WorkspaceImportResultDto result = workspaceEntryService.importEntries(
                new WorkspaceImportRequestDto("""
                        Ix-naq
                        A1-ganar
                        Il gagne.
                        
                        Ha-ix-to
                        DEM-A1-ir
                        Celui-ci va.
                        """, true)
        );

        assertEquals(2, result.importedEntries());
        assertEquals(2, result.totalEntries());
        assertEquals(2, workspaceEntryService.listEntries().size());

        EntryDetailDto first = workspaceEntryService.getEntry(
                workspaceEntryService.listEntries().getFirst().id()
        );

        assertEquals("", first.contextText());
        assertEquals("", first.surfaceText());
        assertEquals("", first.comments());
        assertEquals("Ix-naq", first.rawChujText());
    }

    @Test
    void batchCorrectionShouldProcessAllNonApprovedEntries() {
        workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "",
                "",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "",
                "",
                "",
                "",
                false,
                ""
        ));
        workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                2,
                "",
                "",
                "Ha-ix-to",
                "DEM-A1-ir",
                "Celui-ci va.",
                "",
                "",
                "",
                "",
                true,
                ""
        ));

        BatchCorrectionResultDto result = workspaceEntryService.runCorrectionOnAll(
                new BatchCorrectionRequestDto(false)
        );

        assertEquals(2, result.totalEntries());
        assertEquals(1, result.correctedEntries());
        assertEquals(1, result.skippedApprovedEntries());
    }

    @Test
    void exportRawTextShouldReturnWorkspaceText() {
        workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Context",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                false,
                "# sent_id = 1"
        ));

        TextExportDto result = workspaceEntryService.exportRawText(
                new WorkspaceExportRequestDto(true, false)
        );

        assertEquals("workspace.txt", result.fileName());
        assertTrue(result.content().contains("Ix-naq"));
        assertTrue(result.content().contains("A1-ganar"));
    }

    @Test
    void exportConlluShouldReturnAggregatedPreview() {
        workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Context",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                false,
                "# sent_id = 1\n# text = Ix-naq"
        ));

        TextExportDto result = workspaceEntryService.exportConllu(
                new WorkspaceExportRequestDto(true, false)
        );

        assertEquals("workspace.conllu", result.fileName());
        assertTrue(result.content().contains("# text = Ix-naq"));
    }

    @Test
    void annotationRulesSavedInBackendShouldAffectConlluPreview() {
        ruleService.saveRule(new RuleDetailDto(
                null,
                "Agreement from saved annotation rules",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                20,
                "Demo annotation rule saved in backend.",
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
                                        "Number[subj]", "{agreement_verbs.A.number}"
                                )
                        )
                ),
                """
                        - name: Agreement from saved annotation rules
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
                        """
        ));

        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Context",
                "ix naq",
                "ix-naq",
                "A1-B3-ganar",
                "I beat you",
                "Comment",
                "",
                "",
                "",
                false,
                ""
        ));

        EntryDetailDto corrected = workspaceEntryService.runCorrection(
                new CorrectionRunRequestDto(saved.id(), false)
        );

        assertTrue(corrected.conlluPreview().contains("VERB"));
        assertTrue(corrected.conlluPreview().contains("Pers[subj]=1"));
        assertTrue(corrected.conlluPreview().contains("Number[subj]=Sing"));
    }
}