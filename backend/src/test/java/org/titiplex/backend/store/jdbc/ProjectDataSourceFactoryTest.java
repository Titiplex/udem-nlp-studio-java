package org.titiplex.backend.store.jdbc;

import org.junit.jupiter.api.Test;
import org.titiplex.backend.project.*;
import org.titiplex.backend.service.ProjectSecretService;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectDataSourceFactoryTest {

    @Test
    void createShouldBuildPostgresDatasourceWithSchemaAndSsl() {
        ProjectSecretService secretService = mock(ProjectSecretService.class);
        UUID projectId = UUID.randomUUID();

        when(secretService.resolveSecret(projectId, "secret://main/username"))
                .thenReturn(Optional.of("postgres-user"));
        when(secretService.resolveSecret(projectId, "secret://main/password"))
                .thenReturn(Optional.of("postgres-pass"));

        ProjectDataSourceFactory factory = new ProjectDataSourceFactory(secretService);

        ProjectContext context = projectContext(
                projectId,
                new ProjectSourceDefinition(
                        "main",
                        ProjectSourceKind.POSTGRESQL,
                        "db.example.com",
                        5432,
                        "postgres",
                        "nlp_project_123",
                        "secret://main/username",
                        "secret://main/password",
                        true
                )
        );

        DataSource dataSource = factory.create(context);

        assertNotNull(dataSource);
        assertInstanceOf(org.springframework.jdbc.datasource.DriverManagerDataSource.class, dataSource);

        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                (org.springframework.jdbc.datasource.DriverManagerDataSource) dataSource;

        assertEquals("org.postgresql.Driver", ds.getClass().getName());
        assertEquals(
                "jdbc:postgresql://db.example.com:5432/postgres?currentSchema=nlp_project_123&sslmode=require",
                ds.getUrl()
        );
        assertEquals("postgres-user", ds.getUsername());
        assertEquals("postgres-pass", ds.getPassword());
    }

    @Test
    void createShouldBuildPostgresDatasourceWithoutSslFlag() {
        ProjectSecretService secretService = mock(ProjectSecretService.class);
        UUID projectId = UUID.randomUUID();

        when(secretService.resolveSecret(projectId, "secret://main/username"))
                .thenReturn(Optional.of("postgres-user"));
        when(secretService.resolveSecret(projectId, "secret://main/password"))
                .thenReturn(Optional.of("postgres-pass"));

        ProjectDataSourceFactory factory = new ProjectDataSourceFactory(secretService);

        ProjectContext context = projectContext(
                projectId,
                new ProjectSourceDefinition(
                        "main",
                        ProjectSourceKind.POSTGRESQL,
                        "db.example.com",
                        5432,
                        "postgres",
                        "project_schema",
                        "secret://main/username",
                        "secret://main/password",
                        false
                )
        );

        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                (org.springframework.jdbc.datasource.DriverManagerDataSource) factory.create(context);

        assertEquals(
                "jdbc:postgresql://db.example.com:5432/postgres?currentSchema=project_schema",
                ds.getUrl()
        );
    }

    @Test
    void createShouldFailWhenUsernameSecretMissing() {
        ProjectSecretService secretService = mock(ProjectSecretService.class);
        UUID projectId = UUID.randomUUID();

        when(secretService.resolveSecret(projectId, "secret://main/username"))
                .thenReturn(Optional.empty());

        ProjectDataSourceFactory factory = new ProjectDataSourceFactory(secretService);

        ProjectContext context = projectContext(
                projectId,
                new ProjectSourceDefinition(
                        "main",
                        ProjectSourceKind.POSTGRESQL,
                        "db.example.com",
                        5432,
                        "postgres",
                        "project_schema",
                        "secret://main/username",
                        "secret://main/password",
                        true
                )
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> factory.create(context));
        assertTrue(ex.getMessage().contains("Missing username secret"));
    }

    private ProjectContext projectContext(UUID projectId, ProjectSourceDefinition source) {
        ProjectManifest manifest = new ProjectManifest(
                projectId,
                "Test project",
                "1",
                source.id(),
                List.of(new ProjectMember("pubkey:test", "Tester", ProjectRole.OWNER)),
                List.of(source)
        );

        return new ProjectContext(
                projectId,
                "Test project",
                Path.of("/tmp/project.yaml"),
                manifest,
                new ProjectMember("pubkey:test", "Tester", ProjectRole.OWNER),
                source
        );
    }
}