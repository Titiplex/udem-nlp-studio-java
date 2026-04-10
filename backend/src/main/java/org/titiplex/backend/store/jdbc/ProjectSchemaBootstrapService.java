package org.titiplex.backend.store.jdbc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.project.ProjectSourceDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class ProjectSchemaBootstrapService {

    private final ProjectDataSourceFactory projectDataSourceFactory;

    public ProjectSchemaBootstrapService(ProjectDataSourceFactory projectDataSourceFactory) {
        this.projectDataSourceFactory = projectDataSourceFactory;
    }

    public void ensureProjectSchema(ProjectContext context) {
        DataSource dataSource = projectDataSourceFactory.create(context);
        ProjectSourceDefinition source = context.defaultSource();
        String schema = normalizeSchema(source.schema());

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("create schema if not exists \"" + schema + "\"");
                statement.execute("set search_path to \"" + schema + "\"");
            }
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/project/V1__init_project_schema.sql"));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize project schema '" + schema + "': " + e.getMessage(), e);
        }
    }

    private String normalizeSchema(String schema) {
        if (schema == null || schema.isBlank()) {
            return "public";
        }
        return schema.trim();
    }
}