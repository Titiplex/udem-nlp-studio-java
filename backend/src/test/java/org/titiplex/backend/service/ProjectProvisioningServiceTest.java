package org.titiplex.backend.service;

import org.junit.jupiter.api.Test;
import org.titiplex.backend.dto.ProjectDetailDto;
import org.titiplex.backend.dto.ProjectMemberDto;
import org.titiplex.backend.dto.ProjectSourceDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.jdbc.ProjectConnectionService;
import org.titiplex.backend.store.jdbc.ProjectConnectionTestResult;
import org.titiplex.backend.store.jdbc.ProjectSchemaBootstrapService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectProvisioningServiceTest {

    @Test
    void testActiveProjectConnectionShouldDelegateToConnectionService() {
        ProjectContextService contextService = mock(ProjectContextService.class);
        ProjectConnectionService connectionService = mock(ProjectConnectionService.class);
        ProjectSchemaBootstrapService bootstrapService = mock(ProjectSchemaBootstrapService.class);

        ProjectContext context = mock(ProjectContext.class);
        when(contextService.getRequiredActiveContext()).thenReturn(context);
        when(connectionService.test(context)).thenReturn(new ProjectConnectionTestResult(true, "Connection OK"));

        ProjectProvisioningService service = new ProjectProvisioningService(
                contextService, connectionService, bootstrapService
        );

        ProjectConnectionTestResult result = service.testActiveProjectConnection();

        assertTrue(result.success());
        assertEquals("Connection OK", result.message());
        verify(connectionService).test(context);
    }

    @Test
    void initializeActiveProjectSchemaShouldBootstrapAndReturnProject() {
        ProjectContextService contextService = mock(ProjectContextService.class);
        ProjectConnectionService connectionService = mock(ProjectConnectionService.class);
        ProjectSchemaBootstrapService bootstrapService = mock(ProjectSchemaBootstrapService.class);

        ProjectContext context = mock(ProjectContext.class);
        ProjectDetailDto detail = new ProjectDetailDto(
                UUID.randomUUID(),
                "Project A",
                "1",
                true,
                List.of(new ProjectMemberDto("pubkey:a", "Alice", "OWNER")),
                List.of(new ProjectSourceDto("main", "POSTGRESQL", "db.example.com", 5432, "postgres", "schema_a", true, true))
        );

        when(contextService.getRequiredActiveContext()).thenReturn(context);
        when(connectionService.test(context)).thenReturn(new ProjectConnectionTestResult(true, "Connection OK"));
        when(contextService.getActiveProject()).thenReturn(detail);

        ProjectProvisioningService service = new ProjectProvisioningService(
                contextService, connectionService, bootstrapService
        );

        ProjectDetailDto result = service.initializeActiveProjectSchema();

        assertEquals(detail, result);
        verify(bootstrapService).ensureProjectSchema(context);
    }

    @Test
    void initializeActiveProjectSchemaShouldFailWhenConnectionFails() {
        ProjectContextService contextService = mock(ProjectContextService.class);
        ProjectConnectionService connectionService = mock(ProjectConnectionService.class);
        ProjectSchemaBootstrapService bootstrapService = mock(ProjectSchemaBootstrapService.class);

        ProjectContext context = mock(ProjectContext.class);
        when(contextService.getRequiredActiveContext()).thenReturn(context);
        when(connectionService.test(context)).thenReturn(new ProjectConnectionTestResult(false, "bad credentials"));

        ProjectProvisioningService service = new ProjectProvisioningService(
                contextService, connectionService, bootstrapService
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                service::initializeActiveProjectSchema
        );

        assertTrue(ex.getMessage().contains("Project source connection failed"));
        verify(bootstrapService, never()).ensureProjectSchema(any());
    }
}