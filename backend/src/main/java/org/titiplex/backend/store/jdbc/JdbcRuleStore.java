package org.titiplex.backend.store.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.titiplex.backend.concurrency.ConflictException;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleSummaryDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.RuleStore;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JdbcRuleStore implements RuleStore {

    private final ProjectDataSourceFactory projectDataSourceFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcRuleStore(ProjectDataSourceFactory projectDataSourceFactory) {
        this.projectDataSourceFactory = projectDataSourceFactory;
    }

    @Override
    public List<RuleSummaryDto> listRules(ProjectContext project) {
        return jdbc(project).query(
                "select id, name, kind, subtype, scope, enabled, priority from rules order by priority asc, name asc",
                (rs, rowNum) -> new RuleSummaryDto(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        RuleKind.valueOf(rs.getString("kind")),
                        rs.getString("subtype"),
                        rs.getString("scope"),
                        rs.getBoolean("enabled"),
                        rs.getInt("priority")
                )
        );
    }

    @Override
    public RuleDetailDto getRule(ProjectContext project, UUID id) {
        List<RuleDetailDto> rows = jdbc(project).query(
                "select * from rules where id = ?",
                (rs, rowNum) -> new RuleDetailDto(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        RuleKind.valueOf(rs.getString("kind")),
                        rs.getString("subtype"),
                        rs.getString("scope"),
                        rs.getBoolean("enabled"),
                        rs.getInt("priority"),
                        rs.getString("description"),
                        parsePayload(rs.getString("payload_json")),
                        rs.getString("raw_yaml"),
                        rs.getLong("version"),
                        rs.getString("updated_by"),
                        rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()
                ),
                id
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Rule not found: " + id);
        }
        return rows.getFirst();
    }

    @Override
    public RuleDetailDto saveRule(ProjectContext project, RuleDetailDto dto, SaveOptions options) {
        UUID id = dto.id() != null ? dto.id() : UUID.randomUUID();

        if (dto.id() == null) {
            jdbc(project).update(
                    """
                            insert into rules (
                                id, name, kind, subtype, scope, enabled, priority, description,
                                payload_json, raw_yaml, version, updated_by, updated_at
                            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)
                            """,
                    id,
                    dto.name(),
                    dto.kind().name(),
                    dto.subtype(),
                    dto.scope(),
                    dto.enabled(),
                    dto.priority(),
                    dto.description(),
                    toJson(dto.payload()),
                    dto.rawYaml(),
                    options.updatedBy(),
                    Timestamp.from(Instant.now())
            );
            return getRule(project, id);
        }

        int updated = jdbc(project).update(
                """
                        update rules
                        set name = ?, kind = ?, subtype = ?, scope = ?, enabled = ?, priority = ?, description = ?,
                            payload_json = ?, raw_yaml = ?, version = version + 1, updated_by = ?, updated_at = ?
                        where id = ? and version = ?
                        """,
                dto.name(),
                dto.kind().name(),
                dto.subtype(),
                dto.scope(),
                dto.enabled(),
                dto.priority(),
                dto.description(),
                toJson(dto.payload()),
                dto.rawYaml(),
                options.updatedBy(),
                Timestamp.from(Instant.now()),
                id,
                dto.version() == null ? 0L : dto.version()
        );

        if (updated == 0) {
            throw new ConflictException("Concurrent modification detected for rule " + id);
        }

        return getRule(project, id);
    }

    @Override
    public List<RuleDetailDto> listRulesByKind(ProjectContext project, RuleKind kind) {
        return jdbc(project).query(
                "select * from rules where kind = ? order by priority asc, name asc",
                (rs, rowNum) -> new RuleDetailDto(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        RuleKind.valueOf(rs.getString("kind")),
                        rs.getString("subtype"),
                        rs.getString("scope"),
                        rs.getBoolean("enabled"),
                        rs.getInt("priority"),
                        rs.getString("description"),
                        parsePayload(rs.getString("payload_json")),
                        rs.getString("raw_yaml"),
                        rs.getLong("version"),
                        rs.getString("updated_by"),
                        rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()
                ),
                kind.name()
        );
    }

    private JdbcTemplate jdbc(ProjectContext project) {
        return new JdbcTemplate(projectDataSourceFactory.create(project));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize rule payload: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parsePayload(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse rule payload: " + e.getMessage(), e);
        }
    }
}