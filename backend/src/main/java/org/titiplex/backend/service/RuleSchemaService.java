package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.dto.RuleBuilderSchemaDto;
import org.titiplex.backend.dto.RuleDescriptorDto;
import org.titiplex.backend.mapper.RuleSchemaMapper;
import org.titiplex.rules.registry.RuleSchemaRegistry;

import java.util.List;

@Service
public class RuleSchemaService {

    private final RuleSchemaRegistry ruleSchemaRegistry;
    private final RuleSchemaMapper ruleSchemaMapper;

    public RuleSchemaService(RuleSchemaRegistry ruleSchemaRegistry,
                             RuleSchemaMapper ruleSchemaMapper) {
        this.ruleSchemaRegistry = ruleSchemaRegistry;
        this.ruleSchemaMapper = ruleSchemaMapper;
    }

    public List<RuleDescriptorDto> listRuleDescriptors() {
        return ruleSchemaRegistry.listRuleDescriptors().stream()
                .map(ruleSchemaMapper::toDto)
                .toList();
    }

    public List<RuleBuilderSchemaDto> listBuilderSchemas() {
        return ruleSchemaRegistry.listBuilderSchemas().stream()
                .map(ruleSchemaMapper::toDto)
                .toList();
    }

    public RuleBuilderSchemaDto getBuilderSchema(String kind, String subtype) {
        return ruleSchemaRegistry.findBuilderSchema(kind, subtype)
                .map(ruleSchemaMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Schema not found for kind=" + kind + ", subtype=" + subtype
                ));
    }
}