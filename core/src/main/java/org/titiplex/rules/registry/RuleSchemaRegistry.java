package org.titiplex.rules.registry;

import java.util.List;
import java.util.Optional;

public interface RuleSchemaRegistry {

    List<RuleDescriptor> listRuleDescriptors();

    List<RuleBuilderSchema> listBuilderSchemas();

    Optional<RuleBuilderSchema> findBuilderSchema(String kind, String subtype);
}