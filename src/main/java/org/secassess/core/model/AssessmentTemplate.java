package org.secassess.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.secassess.core.enums.TemplateStatus;
import java.util.List;
import java.util.UUID;

/**
 * Assessment template entity defining a reusable set of security criteria for specific organizations.
 */
@Entity
@Table(name = "assessment_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentTemplate extends BaseAuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "title")
    private String title;

    @Column(name = "version")
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TemplateStatus status;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateCriterion> criteria;
}