package org.titiplex.backend;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("org.titiplex.backend.repository")
@EntityScan(basePackages = "org.titiplex.backend.model")
public class BackendApplication {
}
