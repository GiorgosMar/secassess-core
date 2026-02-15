package org.secassess.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.secassess.core.enums.Severity;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing a specific security requirement or benchmark within an assessment template.
 */
@Entity
@Table(name = "template_criterion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private AssessmentTemplate template;

    @Column(columnDefinition = "section")
    private String section;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "severity")
    private Severity severity;

    @Column(columnDefinition = "weight")
    private BigDecimal weight;
}