package org.secassess.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.secassess.core.enums.Severity;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Individual assessment requirement representing a point-in-time snapshot of a security criterion.
 */
@Entity
@Table(name = "assessment_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @Column(name = "criterion_ref")
    private UUID criterionRef;

    @Column(name = "section")
    private String section;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "score")
    private Integer score;

    @Column(name = "notes")
    private String notes;
}