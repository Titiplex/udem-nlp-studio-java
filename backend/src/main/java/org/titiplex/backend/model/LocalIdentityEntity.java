package org.titiplex.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "local_identity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalIdentityEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String principalId;

    @Column(nullable = false)
    private String displayName;

    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String publicKeyPem;

    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String privateKeyEncryptedPem;
}