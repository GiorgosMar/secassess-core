package org.secassess.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secassess.core.dto.AssessmentDto;
import org.secassess.core.dto.CopyCriteriaRequestDto;
import org.secassess.core.dto.CopyStatsResponseDto;
import org.secassess.core.dto.UpdateStatusRequestDto;
import org.secassess.core.interfaces.AssessmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping(
            value = "/{id}/copy-from-template",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CopyStatsResponseDto> copyCriteria(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CopyCriteriaRequestDto request) {


        CopyStatsResponseDto response = assessmentService.copyCriteriaFromTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssessmentDto> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateStatusRequestDto request) {

        return ResponseEntity.ok(assessmentService.updateStatus(id, request));
    }

    @GetMapping
    public ResponseEntity<Page<AssessmentDto>> getAllAssessments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<AssessmentDto> response = assessmentService.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}