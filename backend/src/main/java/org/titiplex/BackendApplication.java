package org.titiplex;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("org.titiplex.repository")
@EntityScan(basePackages = "org.titiplex.model")
public class BackendApplication {
}
