package org.titiplex.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.titiplex.backend.model.AnnotationSettingsEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AnnotationSettingsRepositoryTest {

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @Test
    void shouldPersistAndReloadAnnotationSettings() {
        annotationSettingsRepository.save(new AnnotationSettingsEntity(
                1L,
                "- VERB\n- NOUN",
                "- Number\n- Pers[subj]",
                "spanish_verbs:\n  - ganar",
                "agreement_verbs:\n  tag_schema: {}",
                "{}"
        ));

        AnnotationSettingsEntity loaded = annotationSettingsRepository.findById(1L).orElseThrow();

        assertTrue(loaded.getPosDefinitionsYaml().contains("VERB"));
        assertTrue(loaded.getFeatDefinitionsYaml().contains("Pers[subj]"));
        assertTrue(loaded.getLexiconsYaml().contains("spanish_verbs"));
    }
}