package org.secassess.core.dto;

import lombok.Builder;
import lombok.Data;
import org.secassess.core.enums.Severity;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data transfer object representing a specific assessment requirement's details and evaluation results for API responses.
 */
@Data
@Builder
public class AssessmentItemDto {
    private UUID id;
    private UUID criterionRef;
    private String section;
    private String text;
    private Severity severity;
    private BigDecimal weight;
    private Integer score;
    private String notes;
}