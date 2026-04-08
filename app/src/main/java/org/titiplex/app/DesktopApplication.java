package org.titiplex.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.titiplex.backend.BackendApplication;

@SpringBootApplication(scanBasePackages = "org.titiplex.app")
@Import(BackendApplication.class)
public class DesktopApplication {
}