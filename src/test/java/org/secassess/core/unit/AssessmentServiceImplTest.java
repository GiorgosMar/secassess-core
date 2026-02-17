package org.secassess.core.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secassess.core.dto.*;
import org.secassess.core.enums.AssessmentStatus;
import org.secassess.core.enums.TemplateStatus;
import org.secassess.core.exception.BusinessValidationException;
import org.secassess.core.mapper.AssessmentMapper;
import org.secassess.core.model.Assessment;
import org.secassess.core.model.AssessmentItem;
import org.secassess.core.model.AssessmentTemplate;
import org.secassess.core.model.TemplateCriterion;
import org.secassess.core.repository.AssessmentItemRepository;
import org.secassess.core.repository.AssessmentRepository;
import org.secassess.core.repository.AssessmentTemplateRepository;
import org.secassess.core.service.AssessmentServiceImpl;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @Mock private AssessmentRepository assessmentRepository;
    @Mock private AssessmentTemplateRepository templateRepository;
    @Mock private AssessmentItemRepository itemRepository;
    @Mock private AssessmentMapper assessmentMapper;

    @InjectMocks private AssessmentServiceImpl assessmentService;

    private UUID assessmentId;
    private UUID templateId;
    private String testTraceId;

    @BeforeEach
    void setUp() {
        assessmentId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        testTraceId = "UNIT-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put("correlationId", testTraceId);
        log.info("--- Starting New Test Case ---");
    }

    @AfterEach
    void tearDown() {
        log.info("--- Finished Test Case ---");
        MDC.clear();
    }

    // ========================================================================
    // TESTS: copyCriteriaFromTemplate
    // ========================================================================

    @Test
    @DisplayName("Copy: Should fail if template is not PUBLISHED")
    void copy_ShouldFail_WhenTemplateNotPublished() {
        log.info("STEP 1: Mocking DRAFT template for ID: {}", templateId);
        Assessment assessment = new Assessment();
        AssessmentTemplate template = new AssessmentTemplate();
        template.setStatus(TemplateStatus.DRAFT);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        CopyCriteriaRequestDto request = new CopyCriteriaRequestDto();
        request.setTemplateId(templateId);

        log.info("STEP 2: Executing copy and asserting BusinessValidationException");
        assertThrows(BusinessValidationException.class, () ->
                assessmentService.copyCriteriaFromTemplate(assessmentId, request));
    }

    @Test
    @DisplayName("Copy: Should succeed with correct stats")
    void copy_ShouldSucceed() {
        log.info("STEP 1: Arranging assessment and published template with 1 criterion");
        Assessment assessment = new Assessment();
        assessment.setItems(new ArrayList<>());

        TemplateCriterion criterion = new TemplateCriterion();
        criterion.setId(UUID.randomUUID());

        AssessmentTemplate template = new AssessmentTemplate();
        template.setStatus(TemplateStatus.PUBLISHED);
        template.setCriteria(List.of(criterion));

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(assessmentMapper.toCopyStatsDto(1, 0, 0, 1)).thenReturn(new CopyStatsResponseDto(1, 0, 0, 1));

        CopyCriteriaRequestDto request = new CopyCriteriaRequestDto();
        request.setTemplateId(templateId);

        log.info("STEP 2: Acting - Running copy process");
        CopyStatsResponseDto stats = assessmentService.copyCriteriaFromTemplate(assessmentId, request);

        log.info("STEP 3: Asserting - Verifying saveAll and stats returned: {}", stats.getCopied());
        assertEquals(1, stats.getCopied());
        verify(itemRepository, times(1)).saveAll(anyList());
    }

    // ========================================================================
    // TESTS: updateStatus
    // ========================================================================

    @Test
    @DisplayName("Status Update: Should fail if COMPLETED but items are unscored")
    void updateStatus_ShouldFail_WhenUnscoredItems() {
        log.info("STEP 1: Arranging assessment with null score items");
        Assessment assessment = new Assessment();
        AssessmentItem item = new AssessmentItem();
        item.setScore(null);
        assessment.setItems(List.of(item));

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        UpdateStatusRequestDto request = new UpdateStatusRequestDto();
        request.setStatus(AssessmentStatus.COMPLETED);

        log.info("STEP 2: Asserting BusinessValidationException for unscored items");
        assertThrows(BusinessValidationException.class, () ->
                assessmentService.updateStatus(assessmentId, request));
    }

    @Test
    @DisplayName("Status Update: Should succeed for valid transition")
    void updateStatus_ShouldSucceed() {
        log.info("STEP 1: Mocking valid assessment save");
        Assessment assessment = new Assessment();
        assessment.setItems(new ArrayList<>());

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRepository.save(any(Assessment.class))).thenReturn(assessment);

        UpdateStatusRequestDto request = new UpdateStatusRequestDto();
        request.setStatus(AssessmentStatus.IN_PROGRESS);

        log.info("STEP 2: Acting - Updating status");
        assessmentService.updateStatus(assessmentId, request);

        log.info("STEP 3: Asserting status changed in entity");
        assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getStatus());
        verify(assessmentRepository).save(assessment);
    }

    // ========================================================================
    // TESTS: findAll
    // ========================================================================

    @Test
    @DisplayName("Find All: Should return paged DTOs")
    void findAll_ShouldReturnPage() {
        log.info("STEP 1: Arranging page of assessments");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assessment> page = new PageImpl<>(List.of(new Assessment()));

        when(assessmentRepository.findAll(pageable)).thenReturn(page);
        when(assessmentMapper.toDtoPage(page)).thenReturn(new PageImpl<>(List.of(new AssessmentDto())));

        log.info("STEP 2: Acting - Fetching page 0");
        Page<AssessmentDto> result = assessmentService.findAll(pageable);

        log.info("STEP 3: Asserting page is not empty");
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }
}