package org.titiplex.app.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.titiplex.backend.concurrency.ConflictException;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.service.*;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Component
public class AppBridge {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleService ruleService;
    private final RuleSchemaService ruleSchemaService;
    private final RuleEditorService ruleEditorService;
    private final WorkspaceEntryService workspaceEntryService;
    private final AnnotationSettingsService annotationSettingsService;
    private final AnnotationConfigComposerService annotationConfigComposerService;
    private final ProjectContextService projectContextService;
    private final ProjectSecretService projectSecretService;
    private final ProjectProvisioningService projectProvisioningService;

    public AppBridge(RuleService ruleService,
                     RuleSchemaService ruleSchemaService,
                     RuleEditorService ruleEditorService,
                     WorkspaceEntryService workspaceEntryService,
                     AnnotationSettingsService annotationSettingsService,
                     AnnotationConfigComposerService annotationConfigComposerService,
                     ProjectContextService projectContextService,
                     ProjectSecretService projectSecretService,
                     ProjectProvisioningService projectProvisioningService) {
        this.ruleService = ruleService;
        this.ruleSchemaService = ruleSchemaService;
        this.ruleEditorService = ruleEditorService;
        this.workspaceEntryService = workspaceEntryService;
        this.annotationSettingsService = annotationSettingsService;
        this.annotationConfigComposerService = annotationConfigComposerService;
        this.projectContextService = projectContextService;
        this.projectSecretService = projectSecretService;
        this.projectProvisioningService = projectProvisioningService;
    }

    public String ping() {
        return safe(() -> "pong");
    }

    public String getAppInfo() {
        return safe(() -> new AppInfo("NLP Studio", "0.1.0"));
    }

    public String listProjects() {
        return safe(projectContextService::listKnownProjects);
    }

    public String getActiveProject() {
        return safe(projectContextService::getActiveProject);
    }

    public String registerProject(String payloadJson) {
        try {
            RegisterProjectRequestDto dto = objectMapper.readValue(payloadJson, RegisterProjectRequestDto.class);
            return write(BridgeResponse.ok(projectContextService.registerProject(Path.of(dto.manifestPath()))));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot register project: " + e.getMessage()));
        }
    }

    public String createProject(String payloadJson) {
        try {
            CreateProjectRequestDto dto = objectMapper.readValue(payloadJson, CreateProjectRequestDto.class);
            return write(BridgeResponse.ok(projectContextService.createProject(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot create project: " + e.getMessage()));
        }
    }

    public String switchProject(String payloadJson) {
        try {
            SwitchProjectRequestDto dto = objectMapper.readValue(payloadJson, SwitchProjectRequestDto.class);
            projectContextService.switchActiveProject(UUID.fromString(dto.projectId()));
            return write(BridgeResponse.ok(projectContextService.getActiveProject()));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot switch project: " + e.getMessage()));
        }
    }

    public String saveProjectSecrets(String payloadJson) {
        try {
            SaveSecretsRequestDto dto = objectMapper.readValue(payloadJson, SaveSecretsRequestDto.class);
            UUID projectId = UUID.fromString(dto.projectId());
            projectSecretService.saveSecret(projectId, dto.usernameRef(), dto.username());
            projectSecretService.saveSecret(projectId, dto.passwordRef(), dto.password());
            return write(BridgeResponse.ok("ok"));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot save secrets: " + e.getMessage()));
        }
    }

    public String testActiveProjectConnection() {
        return safe(() -> {
            var result = projectProvisioningService.testActiveProjectConnection();
            return new ProjectConnectionStatusDto(result.success(), result.message());
        });
    }

    public String initializeActiveProjectSchema() {
        try {
            return write(BridgeResponse.ok(projectProvisioningService.initializeActiveProjectSchema()));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot initialize project schema: " + e.getMessage()));
        }
    }

    public String listRules() {
        return safe(ruleService::listRules);
    }

    public String getRule(String id) {
        try {
            return write(BridgeResponse.ok(ruleService.getRule(UUID.fromString(id))));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot load rule: " + e.getMessage()));
        }
    }

    public String listEntries() {
        return safe(workspaceEntryService::listEntries);
    }

    public String getEntry(String id) {
        try {
            return write(BridgeResponse.ok(workspaceEntryService.getEntry(UUID.fromString(id))));
        } catch (Exception e) {
            return write(BridgeResponse.error("Cannot load entry: " + e.getMessage()));
        }
    }

    public String saveEntry(String payloadJson) {
        try {
            EntryDetailDto dto = objectMapper.readValue(payloadJson, EntryDetailDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.saveEntry(dto)));
        } catch (ConflictException e) {
            return write(BridgeResponse.error("Conflict: " + e.getMessage()));
        } catch (Exception e) {
            return write(BridgeResponse.error("Save entry failed: " + e.getMessage()));
        }
    }

    public String importEntries(String payloadJson) {
        try {
            WorkspaceImportRequestDto dto = objectMapper.readValue(payloadJson, WorkspaceImportRequestDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.importEntries(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Import failed: " + e.getMessage()));
        }
    }

    public String runCorrectionOnAll(String payloadJson) {
        try {
            BatchCorrectionRequestDto dto = objectMapper.readValue(payloadJson, BatchCorrectionRequestDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.runCorrectionOnAll(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Batch correction failed: " + e.getMessage()));
        }
    }

    public String exportRawText(String payloadJson) {
        try {
            WorkspaceExportRequestDto dto = objectMapper.readValue(payloadJson, WorkspaceExportRequestDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.exportRawText(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Raw text export failed: " + e.getMessage()));
        }
    }

    public String exportConllu(String payloadJson) {
        try {
            WorkspaceExportRequestDto dto = objectMapper.readValue(payloadJson, WorkspaceExportRequestDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.exportConllu(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("CoNLL-U export failed: " + e.getMessage()));
        }
    }

    public String getAnnotationSettings() {
        return safe(() -> {
            AnnotationSettingsDto dto = annotationSettingsService.getSettings();
            return new AnnotationSettingsDto(
                    dto.posDefinitionsYaml(),
                    dto.featDefinitionsYaml(),
                    dto.lexiconsYaml(),
                    dto.extractorsYaml(),
                    dto.glossMapYaml(),
                    dto.baseYamlPreview(),
                    annotationConfigComposerService.buildEffectiveYamlPreview(),
                    dto.version(),
                    dto.updatedBy(),
                    dto.updatedAt()
            );
        });
    }

    public String saveAnnotationSettings(String payloadJson) {
        try {
            AnnotationSettingsDto dto = objectMapper.readValue(payloadJson, AnnotationSettingsDto.class);
            AnnotationSettingsDto saved = annotationSettingsService.saveSettings(dto);
            return write(BridgeResponse.ok(new AnnotationSettingsDto(
                    saved.posDefinitionsYaml(),
                    saved.featDefinitionsYaml(),
                    saved.lexiconsYaml(),
                    saved.extractorsYaml(),
                    saved.glossMapYaml(),
                    saved.baseYamlPreview(),
                    annotationConfigComposerService.buildEffectiveYamlPreview(),
                    saved.version(),
                    saved.updatedBy(),
                    saved.updatedAt()
            )));
        } catch (ConflictException e) {
            return write(BridgeResponse.error("Conflict: " + e.getMessage()));
        } catch (Exception e) {
            return write(BridgeResponse.error("Annotation settings save failed: " + e.getMessage()));
        }
    }

    public String listRuleDescriptors() {
        return safe(ruleSchemaService::listRuleDescriptors);
    }

    public String listRuleSchemas() {
        return safe(ruleSchemaService::listBuilderSchemas);
    }

    public String getRuleSchema(String kind, String subtype) {
        try {
            return write(BridgeResponse.ok(ruleSchemaService.getBuilderSchema(kind, subtype)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Schema not found: " + e.getMessage()));
        }
    }

    public String parseRuleYaml(String payloadJson) {
        try {
            RuleDetailDto dto = objectMapper.readValue(payloadJson, RuleDetailDto.class);
            RuleDraftResultDto result = ruleEditorService.parseYamlIntoDraft(dto);
            return write(BridgeResponse.ok(result));
        } catch (Exception e) {
            return write(BridgeResponse.error("YAML parse failed: " + e.getMessage()));
        }
    }

    public String generateRuleYaml(String payloadJson) {
        try {
            RuleDetailDto dto = objectMapper.readValue(payloadJson, RuleDetailDto.class);
            RuleDraftResultDto result = ruleEditorService.generateYamlFromDraft(dto);
            return write(BridgeResponse.ok(result));
        } catch (Exception e) {
            return write(BridgeResponse.error("YAML generation failed: " + e.getMessage()));
        }
    }

    public String validateRule(String payloadJson) {
        try {
            RuleDetailDto dto = objectMapper.readValue(payloadJson, RuleDetailDto.class);
            RuleDraftResultDto result = ruleEditorService.validate(dto);
            return write(BridgeResponse.ok(result));
        } catch (Exception e) {
            return write(BridgeResponse.error("Validation failed: " + e.getMessage()));
        }
    }

    public String saveRule(String payloadJson) {
        try {
            RuleDetailDto dto = objectMapper.readValue(payloadJson, RuleDetailDto.class);
            RuleDetailDto saved = ruleService.saveRule(dto);
            RuleDraftResultDto result = ruleEditorService.validate(saved);
            return write(BridgeResponse.ok(result));
        } catch (ConflictException e) {
            return write(BridgeResponse.error("Conflict: " + e.getMessage()));
        } catch (Exception e) {
            return write(BridgeResponse.error("Save failed: " + e.getMessage()));
        }
    }

    public String runCorrection(String payloadJson) {
        try {
            CorrectionRunRequestDto dto = objectMapper.readValue(payloadJson, CorrectionRunRequestDto.class);
            return write(BridgeResponse.ok(workspaceEntryService.runCorrection(dto)));
        } catch (Exception e) {
            return write(BridgeResponse.error("Correction failed: " + e.getMessage()));
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"Serialization error\"}";
        }
    }

    public record AppInfo(String name, String version) {
    }

    private String safe(Supplier<Object> supplier) {
        try {
            return write(BridgeResponse.ok(supplier.get()));
        } catch (Exception e) {
            System.err.println("Error in bridge: " + e.getMessage());
            return write(BridgeResponse.error(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }
}