package org.titiplex.backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.titiplex.backend.model.WorkspaceEntryEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WorkspaceEntryRepositoryTest {

    @Autowired
    private WorkspaceEntryRepository workspaceEntryRepository;

    @Test
    void findAllByOrderByDocumentOrderAscIdAscShouldSortEntries() {
        WorkspaceEntryEntity second = new WorkspaceEntryEntity(
                UUID.randomUUID(),
                2,
                "",
                "",
                "ha-ix-to",
                "DEM-A1-ir",
                "Celui-ci va.",
                "",
                "",
                "",
                "",
                false,
                ""
        );

        UUID firstIdB = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
        UUID firstIdA = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

        WorkspaceEntryEntity firstB = new WorkspaceEntryEntity(
                firstIdB,
                1,
                "",
                "",
                "ix-b",
                "A1-b",
                "B",
                "",
                "",
                "",
                "",
                false,
                ""
        );

        WorkspaceEntryEntity firstA = new WorkspaceEntryEntity(
                firstIdA,
                1,
                "",
                "",
                "ix-a",
                "A1-a",
                "A",
                "",
                "",
                "",
                "",
                false,
                ""
        );

        workspaceEntryRepository.save(second);
        workspaceEntryRepository.save(firstB);
        workspaceEntryRepository.save(firstA);

        List<WorkspaceEntryEntity> result = workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc();

        assertEquals(3, result.size());
        assertEquals(firstIdA, result.get(0).getId());
        assertEquals(firstIdB, result.get(1).getId());
        assertEquals(2, result.get(2).getDocumentOrder());
    }
}