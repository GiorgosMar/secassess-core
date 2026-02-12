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
@Data
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

    private String section;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private BigDecimal weight;
}