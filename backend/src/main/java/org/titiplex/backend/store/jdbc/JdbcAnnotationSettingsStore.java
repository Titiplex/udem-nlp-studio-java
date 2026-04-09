package org.titiplex.backend.store.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.titiplex.backend.concurrency.ConflictException;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.AnnotationSettingsStore;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class JdbcAnnotationSettingsStore implements AnnotationSettingsStore {

    public static final long SINGLETON_ID = 1L;

    private final ProjectDataSourceFactory projectDataSourceFactory;

    public JdbcAnnotationSettingsStore(ProjectDataSourceFactory projectDataSourceFactory) {
        this.projectDataSourceFactory = projectDataSourceFactory;
    }

    @Override
    public AnnotationSettingsDto getSettings(ProjectContext project) {
        List<AnnotationSettingsDto> rows = jdbc(project).query(
                "select * from annotation_settings where id = ?",
                (rs, rowNum) -> new AnnotationSettingsDto(
                        nvl(rs.getString("pos_definitions_yaml")),
                        nvl(rs.getString("feat_definitions_yaml")),
                        nvl(rs.getString("lexicons_yaml")),
                        nvl(rs.getString("extractors_yaml")),
                        nvl(rs.getString("gloss_map_yaml")),
                        "",
                        "",
                        rs.getLong("version"),
                        nvl(rs.getString("updated_by")),
                        rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant()
                ),
                SINGLETON_ID
        );

        if (!rows.isEmpty()) {
            return rows.getFirst();
        }

        jdbc(project).update(
                """
                        insert into annotation_settings (
                            id, pos_definitions_yaml, feat_definitions_yaml, lexicons_yaml, extractors_yaml, gloss_map_yaml,
                            version, updated_by, updated_at
                        ) values (?, '', '', '', '', '', 0, ?, ?)
                        """,
                SINGLETON_ID,
                project.localMember().principalId(),
                Timestamp.from(Instant.now())
        );

        return getSettings(project);
    }

    @Override
    public AnnotationSettingsDto saveSettings(ProjectContext project, AnnotationSettingsDto dto, SaveOptions options) {
        int updated = jdbc(project).update(
                """
                        update annotation_settings
                        set pos_definitions_yaml = ?, feat_definitions_yaml = ?, lexicons_yaml = ?, extractors_yaml = ?, gloss_map_yaml = ?,
                            version = version + 1, updated_by = ?, updated_at = ?
                        where id = ? and version = ?
                        """,
                nvl(dto.posDefinitionsYaml()),
                nvl(dto.featDefinitionsYaml()),
                nvl(dto.lexiconsYaml()),
                nvl(dto.extractorsYaml()),
                nvl(dto.glossMapYaml()),
                options.updatedBy(),
                Timestamp.from(Instant.now()),
                SINGLETON_ID,
                dto.version() == null ? 0L : dto.version()
        );

        if (updated == 0) {
            throw new ConflictException("Concurrent modification detected for annotation settings");
        }

        return getSettings(project);
    }

    private JdbcTemplate jdbc(ProjectContext project) {
        return new JdbcTemplate(projectDataSourceFactory.create(project));
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}