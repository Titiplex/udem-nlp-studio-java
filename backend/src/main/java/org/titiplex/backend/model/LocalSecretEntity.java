package org.titiplex.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "local_secret")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalSecretEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String secretRef;

    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String secretJsonEncrypted;
}