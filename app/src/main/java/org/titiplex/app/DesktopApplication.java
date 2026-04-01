package org.titiplex.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.titiplex.backend.BackendApplication;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "org.titiplex.app",
                "org.titiplex.backend"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = BackendApplication.class
        )
)
public class DesktopApplication {
}