package org.secassess.core.interfaces;

import org.secassess.core.dto.AssessmentDto;
import org.secassess.core.dto.CopyCriteriaRequestDto;
import org.secassess.core.dto.CopyStatsResponseDto;
import org.secassess.core.dto.UpdateStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AssessmentService {
    CopyStatsResponseDto copyCriteriaFromTemplate(UUID assessmentId, CopyCriteriaRequestDto request);

    AssessmentDto updateStatus(UUID assessmentId, UpdateStatusRequestDto request);

    Page<AssessmentDto> findAll(Pageable pageable);
}