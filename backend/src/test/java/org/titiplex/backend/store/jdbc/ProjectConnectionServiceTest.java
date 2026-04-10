package org.titiplex.backend.store.jdbc;

import org.junit.jupiter.api.Test;
import org.titiplex.backend.project.*;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectConnectionServiceTest {

    @Test
    void testShouldReturnSuccessWhenConnectionIsValid() throws Exception {
        ProjectDataSourceFactory factory = mock(ProjectDataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(factory.create(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        ProjectConnectionService service = new ProjectConnectionService(factory);

        ProjectConnectionTestResult result = service.test(projectContext());

        assertTrue(result.success());
        assertEquals("Connection OK", result.message());
    }

    @Test
    void testShouldReturnFailureWhenConnectionIsInvalid() throws Exception {
        ProjectDataSourceFactory factory = mock(ProjectDataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(factory.create(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        ProjectConnectionService service = new ProjectConnectionService(factory);

        ProjectConnectionTestResult result = service.test(projectContext());

        assertFalse(result.success());
        assertEquals("Database connection is not valid", result.message());
    }

    @Test
    void testShouldReturnFailureWhenConnectionThrows() throws Exception {
        ProjectDataSourceFactory factory = mock(ProjectDataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);

        when(factory.create(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new RuntimeException("boom"));

        ProjectConnectionService service = new ProjectConnectionService(factory);

        ProjectConnectionTestResult result = service.test(projectContext());

        assertFalse(result.success());
        assertTrue(result.message().contains("boom"));
    }

    private ProjectContext projectContext() {
        UUID projectId = UUID.randomUUID();
        ProjectSourceDefinition source = new ProjectSourceDefinition(
                "main",
                ProjectSourceKind.POSTGRESQL,
                "db.example.com",
                5432,
                "postgres",
                "project_schema",
                "secret://main/username",
                "secret://main/password",
                true
        );

        ProjectManifest manifest = new ProjectManifest(
                projectId,
                "Test project",
                "1",
                "main",
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