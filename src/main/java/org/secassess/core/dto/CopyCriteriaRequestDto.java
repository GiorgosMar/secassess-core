package org.secassess.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.secassess.core.validators.SemVer;

import java.util.List;
import java.util.UUID;

/**
 * Data transfer object encapsulating the parameters required to initiate a criteria copy operation from a template to an assessment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyCriteriaRequestDto {

    @NotBlank(message = "Organization slug is required")
    private String sourceOrganizationSlug;

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    @SemVer(message = "The template version must follow Semantic Versioning")
    private String targetVersion;

    private List<String> includeSections;

    private boolean overwriteExisting;
}