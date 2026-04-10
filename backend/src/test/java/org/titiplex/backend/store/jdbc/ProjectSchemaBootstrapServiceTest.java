package org.titiplex.backend.store.jdbc;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.titiplex.backend.project.*;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectSchemaBootstrapServiceTest {

    @Test
    void ensureProjectSchemaShouldCreateSchemaSetSearchPathAndRunScript() throws Exception {
        ProjectDataSourceFactory factory = mock(ProjectDataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);

        when(factory.create(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        ProjectSchemaBootstrapService service = new ProjectSchemaBootstrapService(factory);

        try (MockedStatic<ScriptUtils> scriptUtils = mockStatic(ScriptUtils.class)) {
            service.ensureProjectSchema(projectContext("custom_schema"));

            verify(statement).execute("create schema if not exists \"custom_schema\"");
            verify(statement).execute("set search_path to \"custom_schema\"");
            scriptUtils.verify(() ->
                    ScriptUtils.executeSqlScript(eq(connection), any(ClassPathResource.class)));
        }
    }

    @Test
    void ensureProjectSchemaShouldWrapFailures() throws Exception {
        ProjectDataSourceFactory factory = mock(ProjectDataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);

        when(factory.create(any())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new RuntimeException("cannot connect"));

        ProjectSchemaBootstrapService service = new ProjectSchemaBootstrapService(factory);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.ensureProjectSchema(projectContext("custom_schema"))
        );

        assert ex.getMessage().contains("Cannot initialize project schema");
    }

    private ProjectContext projectContext(String schema) {
        UUID projectId = UUID.randomUUID();
        ProjectSourceDefinition source = new ProjectSourceDefinition(
                "main",
                ProjectSourceKind.POSTGRESQL,
                "db.example.com",
                5432,
                "postgres",
                schema,
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