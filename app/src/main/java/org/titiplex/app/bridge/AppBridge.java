package org.titiplex.app.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleDraftResultDto;
import org.titiplex.backend.service.RuleEditorService;
import org.titiplex.backend.service.RuleSchemaService;
import org.titiplex.backend.service.RuleService;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class AppBridge {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleService ruleService;
    private final RuleSchemaService ruleSchemaService;
    private final RuleEditorService ruleEditorService;

    public AppBridge(RuleService ruleService,
                     RuleSchemaService ruleSchemaService,
                     RuleEditorService ruleEditorService) {
        this.ruleService = ruleService;
        this.ruleSchemaService = ruleSchemaService;
        this.ruleEditorService = ruleEditorService;
    }

    public String ping() {
        return safe(() -> "pong");
    }

    public String getAppInfo() {
        return safe(() -> new AppInfo("NLP Studio", "0.1.0"));
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
        } catch (Exception e) {
            return write(BridgeResponse.error("Save failed: " + e.getMessage()));
        }
    }

    public String runCorrection(String payloadJson) {
        return write(BridgeResponse.ok("not implemented yet"));
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