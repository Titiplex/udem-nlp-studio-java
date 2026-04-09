package org.titiplex.backend.store.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.titiplex.backend.concurrency.ConflictException;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.dto.EntryDetailDto;
import org.titiplex.backend.dto.EntrySummaryDto;
import org.titiplex.backend.dto.WorkspaceImportRequestDto;
import org.titiplex.backend.dto.WorkspaceImportResultDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.WorkspaceEntryStore;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcWorkspaceEntryStore implements WorkspaceEntryStore {

    private final ProjectDataSourceFactory projectDataSourceFactory;

    public JdbcWorkspaceEntryStore(ProjectDataSourceFactory projectDataSourceFactory) {
        this.projectDataSourceFactory = projectDataSourceFactory;
    }

    @Override
    public List<EntrySummaryDto> listEntries(ProjectContext project) {
        JdbcTemplate jdbc = jdbc(project);
        return jdbc.query(
                """
                        select id, document_order, raw_chuj_text, raw_gloss_text, translation, approved,
                               (coalesce(corrected_chuj_text, '') <> '' or coalesce(corrected_gloss_text, '') <> '' or coalesce(corrected_translation, '') <> '') as has_corrected
                        from workspace_entries
                        order by document_order asc, id asc
                        """,
                (rs, rowNum) -> new EntrySummaryDto(
                        UUID.fromString(rs.getString("id")),
                        rs.getInt("document_order"),
                        nvl(rs.getString("raw_chuj_text")),
                        nvl(rs.getString("raw_gloss_text")),
                        nvl(rs.getString("translation")),
                        rs.getBoolean("approved"),
                        rs.getBoolean("has_corrected")
                )
        );
    }

    @Override
    public EntryDetailDto getEntry(ProjectContext project, UUID id) {
        JdbcTemplate jdbc = jdbc(project);
        List<EntryDetailDto> rows = jdbc.query(
                """
                        select id, document_order, raw_chuj_text, raw_gloss_text, translation,
                               corrected_chuj_text, corrected_gloss_text, corrected_translation,
                               approved, conllu_preview, version, updated_by, updated_at
                        from workspace_entries
                        where id = ?
                        """,
                (rs, rowNum) -> mapDetail(rs),
                id
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Entry not found: " + id);
        }
        return rows.getFirst();
    }

    @Override
    public EntryDetailDto saveEntry(ProjectContext project, EntryDetailDto dto, SaveOptions options) {
        JdbcTemplate jdbc = jdbc(project);
        UUID id = dto.id() != null ? dto.id() : UUID.randomUUID();
        long expectedVersion = dto.version() == null ? 0L : dto.version();

        if (dto.id() == null) {
            jdbc.update(
                    """
                            insert into workspace_entries (
                                id, document_order, raw_chuj_text, raw_gloss_text, translation,
                                corrected_chuj_text, corrected_gloss_text, corrected_translation,
                                approved, conllu_preview, version, updated_by, updated_at
                            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    id,
                    dto.documentOrder(),
                    nvl(dto.rawChujText()),
                    nvl(dto.rawGlossText()),
                    nvl(dto.translation()),
                    nvl(dto.correctedChujText()),
                    nvl(dto.correctedGlossText()),
                    nvl(dto.correctedTranslation()),
                    dto.approved(),
                    nvl(dto.conlluPreview()),
                    0L,
                    options.updatedBy(),
                    Timestamp.from(Instant.now())
            );
            return getEntry(project, id);
        }

        int updated = jdbc.update(
                """
                        update workspace_entries
                        set document_order = ?,
                            raw_chuj_text = ?,
                            raw_gloss_text = ?,
                            translation = ?,
                            corrected_chuj_text = ?,
                            corrected_gloss_text = ?,
                            corrected_translation = ?,
                            approved = ?,
                            conllu_preview = ?,
                            version = version + 1,
                            updated_by = ?,
                            updated_at = ?
                        where id = ? and version = ?
                        """,
                dto.documentOrder(),
                nvl(dto.rawChujText()),
                nvl(dto.rawGlossText()),
                nvl(dto.translation()),
                nvl(dto.correctedChujText()),
                nvl(dto.correctedGlossText()),
                nvl(dto.correctedTranslation()),
                dto.approved(),
                nvl(dto.conlluPreview()),
                options.updatedBy(),
                Timestamp.from(Instant.now()),
                id,
                expectedVersion
        );

        if (updated == 0) {
            throw new ConflictException("Concurrent modification detected for entry " + id);
        }

        return getEntry(project, id);
    }

    @Override
    public WorkspaceImportResultDto importEntries(ProjectContext project, WorkspaceImportRequestDto request) {
        throw new UnsupportedOperationException("Import is handled by service layer after parsing blocks");
    }

    public UUID insertParsedEntry(ProjectContext project,
                                  int documentOrder,
                                  String rawChujText,
                                  String rawGlossText,
                                  String translation,
                                  String updatedBy) {
        UUID id = UUID.randomUUID();
        jdbc(project).update(
                """
                        insert into workspace_entries (
                            id, document_order, raw_chuj_text, raw_gloss_text, translation,
                            corrected_chuj_text, corrected_gloss_text, corrected_translation,
                            approved, conllu_preview, version, updated_by, updated_at
                        ) values (?, ?, ?, ?, ?, '', '', '', false, '', 0, ?, ?)
                        """,
                id,
                documentOrder,
                nvl(rawChujText),
                nvl(rawGlossText),
                nvl(translation),
                updatedBy,
                Timestamp.from(Instant.now())
        );
        return id;
    }

    private EntryDetailDto mapDetail(ResultSet rs) throws java.sql.SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        return new EntryDetailDto(
                UUID.fromString(rs.getString("id")),
                rs.getInt("document_order"),
                nvl(rs.getString("raw_chuj_text")),
                nvl(rs.getString("raw_gloss_text")),
                nvl(rs.getString("translation")),
                nvl(rs.getString("corrected_chuj_text")),
                nvl(rs.getString("corrected_gloss_text")),
                nvl(rs.getString("corrected_translation")),
                rs.getBoolean("approved"),
                nvl(rs.getString("conllu_preview")),
                rs.getLong("version"),
                nvl(rs.getString("updated_by")),
                ts == null ? null : ts.toInstant()
        );
    }

    private JdbcTemplate jdbc(ProjectContext project) {
        DataSource ds = projectDataSourceFactory.create(project);
        return new JdbcTemplate(ds);
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}