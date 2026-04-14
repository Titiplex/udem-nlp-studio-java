package org.titiplex.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "workspace_entries")
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceEntryEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private int documentOrder;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String contextText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String surfaceText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawChujText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawGlossText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String translation;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String comments;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String correctedChujText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String correctedGlossText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String correctedTranslation;

    @Column(nullable = false)
    private boolean approved;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String conlluPreview;
}