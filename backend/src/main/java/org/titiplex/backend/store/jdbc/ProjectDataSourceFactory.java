package org.titiplex.backend.store.jdbc;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.project.ProjectSourceDefinition;
import org.titiplex.backend.project.ProjectSourceKind;
import org.titiplex.backend.service.ProjectSecretService;

import javax.sql.DataSource;

@Component
public class ProjectDataSourceFactory {

    private final ProjectSecretService projectSecretService;

    public ProjectDataSourceFactory(ProjectSecretService projectSecretService) {
        this.projectSecretService = projectSecretService;
    }

    public DataSource create(ProjectContext context) {
        ProjectSourceDefinition source = context.defaultSource();
        if (source.kind() != ProjectSourceKind.POSTGRESQL) {
            throw new IllegalArgumentException("Unsupported source kind: " + source.kind());
        }

        String username = projectSecretService.resolveSecret(context.projectId(), source.usernameRef())
                .orElseThrow(() -> new IllegalStateException("Missing username secret for project " + context.name()));
        String password = projectSecretService.resolveSecret(context.projectId(), source.passwordRef())
                .orElseThrow(() -> new IllegalStateException("Missing password secret for project " + context.name()));

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(source.host())
                .append(":")
                .append(source.port())
                .append("/")
                .append(source.database());

        jdbcUrl.append("?currentSchema=").append(source.schema());
        if (source.ssl()) {
            jdbcUrl.append("&sslmode=require");
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl.toString());
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}