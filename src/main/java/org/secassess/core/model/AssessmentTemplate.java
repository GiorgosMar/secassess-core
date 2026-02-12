package org.secassess.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.secassess.core.enums.TemplateStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Assessment template entity defining a reusable set of security criteria for specific organizations.
 */
@Entity
@Table(name = "assessment_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    private String title;
    private String version;

    @Enumerated(EnumType.STRING)
    private TemplateStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateCriterion> criteria;
}