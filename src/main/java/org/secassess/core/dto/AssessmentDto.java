package org.secassess.core.dto;

import lombok.Builder;
import lombok.Data;
import org.secassess.core.enums.AssessmentStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object representing the comprehensive state of an assessment, including its nested items, for API communication.
 */
@Data
@Builder
public class AssessmentDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private Long projectId;
    private String title;
    private AssessmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AssessmentItemDto> items;
}