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
@Data
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

    private String section;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private BigDecimal weight;
    private Integer score;
    private String notes;
}