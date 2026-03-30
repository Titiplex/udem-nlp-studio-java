package org.titiplex.app.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.titiplex.backend.service.RuleSchemaService;
import org.titiplex.backend.service.RuleService;

@Component
public class AppBridge {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleService ruleService;
    private final RuleSchemaService ruleSchemaService;

    public AppBridge(RuleService ruleService,
                     RuleSchemaService ruleSchemaService) {
        this.ruleService = ruleService;
        this.ruleSchemaService = ruleSchemaService;
    }

    public String ping() {
        return write(BridgeResponse.ok("pong"));
    }

    public String getAppInfo() {
        return write(BridgeResponse.ok(new AppInfo("NLP Studio", "0.1.0")));
    }

    public String listRules() {
        return write(BridgeResponse.ok(ruleService.listRules()));
    }

    public String listRuleDescriptors() {
        return write(BridgeResponse.ok(ruleSchemaService.listRuleDescriptors()));
    }

    public String listRuleSchemas() {
        return write(BridgeResponse.ok(ruleSchemaService.listBuilderSchemas()));
    }

    public String getRuleSchema(String kind, String subtype) {
        return write(BridgeResponse.ok(ruleSchemaService.getBuilderSchema(kind, subtype)));
    }

    public String saveRule(String payloadJson) {
        return write(BridgeResponse.ok("not implemented yet"));
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
}