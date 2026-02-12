package org.secassess.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.secassess.core.enums.AssessmentStatus;

/**
 * Data transfer object used for submitting state transition requests to update an assessment's current status.
 */
@Data
public class UpdateStatusRequestDto {

    @NotNull(message = "Assessment status is required")
    private AssessmentStatus status;
}