package org.secassess.core.dto;

import lombok.Builder;
import lombok.Data;
import org.secassess.core.enums.AssessmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object representing the comprehensive state of an assessment, including its nested items, for API communication.
 */
@Data
@Builder
public class AssessmentDto {
    private UUID id;
    private Long projectId;
    private String title;
    private AssessmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AssessmentItemDto> items;
}