package org.titiplex.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.titiplex.backend.domain.rule.RuleKind;

import java.util.UUID;

@Data
@Entity
@Table(name = "rules")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RuleEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RuleKind kind;

    @Column(nullable = false, length = 100)
    private String subtype;
    @Column(nullable = false, length = 50)
    private String scope;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false)
    private int priority;

    @Column(length = 4000)
    private String description;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String payloadJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawYaml;
}