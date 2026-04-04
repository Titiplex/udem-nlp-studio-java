package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.dto.CorrectionRunRequestDto;
import org.titiplex.backend.dto.EntryDetailDto;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class WorkspaceEntryServiceTest {

    @Autowired
    private WorkspaceEntryService workspaceEntryService;

    @Autowired
    private WorkspaceEntryRepository workspaceEntryRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @BeforeEach
    void setUp() {
        workspaceEntryRepository.deleteAll();
        ruleRepository.deleteAll();
    }

    @Test
    void saveAndListEntriesShouldWork() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Ix naq",
                "A1 ganar",
                "Il gagne.",
                "",
                "",
                "",
                false,
                ""
        ));

        assertNotNull(saved.id());
        assertEquals(1, workspaceEntryService.listEntries().size());
        assertEquals("Ix naq", workspaceEntryService.listEntries().getFirst().rawChujText());
    }

    @Test
    void runCorrectionWithoutRulesShouldStillPopulatePreview() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Ix naq",
                "A1 ganar",
                "Il gagne.",
                "",
                "",
                "",
                false,
                ""
        ));

        EntryDetailDto corrected = workspaceEntryService.runCorrection(
                new CorrectionRunRequestDto(saved.id(), false)
        );

        assertEquals("Ix naq", corrected.correctedChujText());
        assertEquals("A1 ganar", corrected.correctedGlossText());
        assertFalse(corrected.conlluPreview().isBlank());
    }

    @Test
    void approvedEntryShouldNotBeForcedByDefault() {
        EntryDetailDto saved = workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                1,
                "Ix naq",
                "A1 ganar",
                "Il gagne.",
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
        assertEquals("manual-preview", result.conlluPreview());
    }
}