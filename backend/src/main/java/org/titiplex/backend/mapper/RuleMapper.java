package org.titiplex.backend.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.titiplex.backend.domain.rule.RuleDefinition;
import org.titiplex.backend.model.RuleEntity;

import java.util.Map;

@Component
public class RuleMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuleDefinition toDomain(RuleEntity entity) {
        return new RuleDefinition(
                entity.getId(),
                entity.getName(),
                entity.getKind(),
                entity.getSubtype(),
                entity.getScope(),
                entity.isEnabled(),
                entity.getPriority(),
                entity.getDescription(),
                readPayload(entity.getPayloadJson()),
                entity.getRawYaml()
        );
    }

    public RuleEntity toEntity(RuleDefinition definition) {
        RuleEntity entity = new RuleEntity();
        entity.setId(definition.id());
        entity.setName(definition.name());
        entity.setKind(definition.kind());
        entity.setSubtype(definition.subtype());
        entity.setScope(definition.scope());
        entity.setEnabled(definition.enabled());
        entity.setPriority(definition.priority());
        entity.setDescription(definition.description());
        entity.setPayloadJson(writePayload(definition.payload()));
        entity.setRawYaml(definition.rawYaml());
        return entity;
    }

    private Map<String, Object> readPayload(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Invalid rule payload JSON", e);
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            if (payload == null || payload.isEmpty()) {
                return "{}";
            }
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize rule payload", e);
        }
    }
}