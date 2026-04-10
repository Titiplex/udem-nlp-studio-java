package org.titiplex.backend.store.jdbc;

import org.springframework.stereotype.Service;
import org.titiplex.backend.project.ProjectContext;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class ProjectConnectionService {

    private final ProjectDataSourceFactory projectDataSourceFactory;

    public ProjectConnectionService(ProjectDataSourceFactory projectDataSourceFactory) {
        this.projectDataSourceFactory = projectDataSourceFactory;
    }

    public ProjectConnectionTestResult test(ProjectContext context) {
        try {
            DataSource dataSource = projectDataSourceFactory.create(context);
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(5);
                if (!valid) {
                    return new ProjectConnectionTestResult(false, "Database connection is not valid");
                }
                return new ProjectConnectionTestResult(true, "Connection OK");
            }
        } catch (Exception e) {
            return new ProjectConnectionTestResult(false, e.getMessage());
        }
    }
}