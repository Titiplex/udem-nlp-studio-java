package org.titiplex.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.titiplex.rules.registry.DefaultRuleSchemaRegistry;
import org.titiplex.rules.registry.RuleSchemaRegistry;

@Configuration
public class RegistryConfig {

    @Bean
    public RuleSchemaRegistry ruleSchemaRegistry() {
        return new DefaultRuleSchemaRegistry();
    }
}