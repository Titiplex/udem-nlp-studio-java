package org.titiplex.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRegistryEntity {

    @Id
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2048)
    private String manifestPath;

    @Column(nullable = false)
    private Instant lastOpenedAt;

    @Column(nullable = false)
    private boolean favorite;
}