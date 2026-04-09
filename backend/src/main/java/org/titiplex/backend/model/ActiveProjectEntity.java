package org.titiplex.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "active_project")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveProjectEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private UUID activeProjectId;
}