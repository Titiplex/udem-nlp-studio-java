package org.titiplex.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "annotation_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationSettingsEntity {

    @Id
    private Long id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String posDefinitionsYaml;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String featDefinitionsYaml;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String lexiconsYaml;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String extractorsYaml;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String glossMapYaml;
}