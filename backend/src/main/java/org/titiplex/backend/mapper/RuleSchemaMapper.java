package org.titiplex.backend.mapper;

import org.springframework.stereotype.Component;
import org.titiplex.backend.dto.FieldDescriptorDto;
import org.titiplex.backend.dto.RuleBuilderSchemaDto;
import org.titiplex.backend.dto.RuleDescriptorDto;
import org.titiplex.rules.registry.FieldDescriptor;
import org.titiplex.rules.registry.RuleBuilderSchema;
import org.titiplex.rules.registry.RuleDescriptor;

@Component
public class RuleSchemaMapper {

    public RuleDescriptorDto toDto(RuleDescriptor descriptor) {
        return new RuleDescriptorDto(
                descriptor.kind(),
                descriptor.subtype(),
                descriptor.label(),
                descriptor.description()
        );
    }

    public RuleBuilderSchemaDto toDto(RuleBuilderSchema schema) {
        return new RuleBuilderSchemaDto(
                schema.kind(),
                schema.subtype(),
                schema.label(),
                schema.description(),
                schema.fields().stream().map(this::toDto).toList()
        );
    }

    public FieldDescriptorDto toDto(FieldDescriptor field) {
        return new FieldDescriptorDto(
                field.key(),
                field.label(),
                field.type().name(),
                field.required(),
                field.repeatable(),
                field.placeholder(),
                field.helpText(),
                field.enumValues(),
                field.defaultValue(),
                field.nestedFields().stream().map(this::toDto).toList()
        );
    }
}