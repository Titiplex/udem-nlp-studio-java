package org.titiplex.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.titiplex.backend.dto.CreateProjectRequestDto;
import org.titiplex.backend.dto.ProjectDetailDto;
import org.titiplex.backend.model.ActiveProjectEntity;
import org.titiplex.backend.model.LocalIdentityEntity;
import org.titiplex.backend.model.ProjectRegistryEntity;
import org.titiplex.backend.project.ManifestProjectLoader;
import org.titiplex.backend.repository.ActiveProjectRepository;
import org.titiplex.backend.repository.LocalIdentityRepository;
import org.titiplex.backend.repository.ProjectRegistryRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectContextServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createProjectShouldGenerateSchemaWhenMissingAndWriteManifest() throws Exception {
        ProjectRegistryRepository projectRegistryRepository = mock(ProjectRegistryRepository.class);
        ActiveProjectRepository activeProjectRepository = mock(ActiveProjectRepository.class);
        LocalIdentityRepository localIdentityRepository = mock(LocalIdentityRepository.class);
        ProjectSecretService projectSecretService = mock(ProjectSecretService.class);

        ManifestProjectLoader loader = new ManifestProjectLoader();

        LocalIdentityEntity identity = new LocalIdentityEntity(
                1L,
                "pubkey:test-user",
                "Test User",
                "PUBLIC",
                "PRIVATE"
        );

        Map<UUID, ProjectRegistryEntity> savedProjects = new HashMap<>();
        final UUID[] activeProjectId = new UUID[1];

        when(localIdentityRepository.findById(1L)).thenReturn(Optional.of(identity));

        when(projectRegistryRepository.save(any(ProjectRegistryEntity.class))).thenAnswer(invocation -> {
            ProjectRegistryEntity entity = invocation.getArgument(0);
            savedProjects.put(entity.getProjectId(), entity);
            return entity;
        });

        when(projectRegistryRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return Optional.ofNullable(savedProjects.get(id));
        });

        when(projectRegistryRepository.findAllByOrderByLastOpenedAtDescNameAsc()).thenAnswer(invocation ->
                savedProjects.values().stream()
                        .sorted(Comparator.comparing(ProjectRegistryEntity::getLastOpenedAt).reversed())
                        .toList()
        );

        when(activeProjectRepository.save(any(ActiveProjectEntity.class))).thenAnswer(invocation -> {
            ActiveProjectEntity entity = invocation.getArgument(0);
            activeProjectId[0] = entity.getActiveProjectId();
            return entity;
        });

        when(activeProjectRepository.findById(1L)).thenAnswer(invocation -> {
            if (activeProjectId[0] == null) {
                return Optional.empty();
            }
            return Optional.of(new ActiveProjectEntity(1L, activeProjectId[0]));
        });

        ProjectContextService service = new ProjectContextService(
                projectRegistryRepository,
                activeProjectRepository,
                localIdentityRepository,
                loader,
                projectSecretService
        );

        Path projectDir = tempDir.resolve("project-a");

        ProjectDetailDto detail = service.createProject(new CreateProjectRequestDto(
                "Corpus Chuj Principal",
                projectDir.toString(),
                "main",
                "db.example.com",
                5432,
                "postgres",
                "",
                true,
                "postgres-user",
                "postgres-pass"
        ));

        assertNotNull(detail.projectId());
        assertEquals("Corpus Chuj Principal", detail.name());
        assertTrue(detail.active());
        assertEquals(1, detail.members().size());
        assertEquals(1, detail.sources().size());

        assertTrue(detail.sources().getFirst().schema().startsWith("nlp_corpus_chuj_principal_"));

        Path manifestPath = projectDir.resolve("project.yaml");
        assertTrue(Files.exists(manifestPath));

        String yaml = Files.readString(manifestPath);
        assertTrue(yaml.contains("name: Corpus Chuj Principal"));
        assertTrue(yaml.contains("host: db.example.com"));
        assertTrue(yaml.contains("database: postgres"));
        assertTrue(yaml.contains("kind: POSTGRESQL"));
        assertTrue(yaml.contains("schema: nlp_corpus_chuj_principal_"));

        verify(projectSecretService).saveSecret(eq(detail.projectId()), eq("secret://main/username"), eq("postgres-user"));
        verify(projectSecretService).saveSecret(eq(detail.projectId()), eq("secret://main/password"), eq("postgres-pass"));
    }

    @Test
    void listKnownProjectsShouldIncludeSchemaInSourceLabel() {
        ProjectRegistryRepository projectRegistryRepository = mock(ProjectRegistryRepository.class);
        ActiveProjectRepository activeProjectRepository = mock(ActiveProjectRepository.class);
        LocalIdentityRepository localIdentityRepository = mock(LocalIdentityRepository.class);
        ProjectSecretService projectSecretService = mock(ProjectSecretService.class);

        ManifestProjectLoader loader = new ManifestProjectLoader();

        when(activeProjectRepository.findById(1L))
                .thenReturn(Optional.of(new ActiveProjectEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"))));

        Path manifestPath = tempDir.resolve("project.yaml");
        assertDoesNotThrow(() -> Files.writeString(manifestPath, """
                projectId: "11111111-1111-1111-1111-111111111111"
                name: "Project A"
                version: "1"
                defaultSourceId: "main"
                members:
                  - principalId: "pubkey:test-user"
                    displayName: "Test User"
                    role: "OWNER"
                sources:
                  - id: "main"
                    kind: "POSTGRESQL"
                    host: "db.example.com"
                    port: 5432
                    database: "postgres"
                    schema: "schema_a"
                    usernameRef: "secret://main/username"
                    passwordRef: "secret://main/password"
                    ssl: true
                """));

        when(projectRegistryRepository.findAllByOrderByLastOpenedAtDescNameAsc())
                .thenReturn(List.of(
                        new ProjectRegistryEntity(
                                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                                "Project A",
                                manifestPath.toString(),
                                Instant.now(),
                                false
                        )
                ));

        ProjectContextService service = new ProjectContextService(
                projectRegistryRepository,
                activeProjectRepository,
                localIdentityRepository,
                loader,
                projectSecretService
        );

        var projects = service.listKnownProjects();

        assertEquals(1, projects.size());
        assertEquals("db.example.com/postgres [schema_a]", projects.getFirst().sourceLabel());
        assertTrue(projects.getFirst().active());
    }
}