package org.secassess.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secassess.core.dto.*;
import org.secassess.core.enums.AssessmentStatus;
import org.secassess.core.enums.TemplateStatus;
import org.secassess.core.exception.BusinessValidationException;
import org.secassess.core.exception.ResourceNotFoundException;
import org.secassess.core.interfaces.AssessmentService;
import org.secassess.core.mapper.AssessmentMapper;
import org.secassess.core.model.Assessment;
import org.secassess.core.model.AssessmentItem;
import org.secassess.core.model.AssessmentTemplate;
import org.secassess.core.model.TemplateCriterion;
import org.secassess.core.repository.AssessmentItemRepository;
import org.secassess.core.repository.AssessmentRepository;
import org.secassess.core.repository.AssessmentTemplateRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentTemplateRepository templateRepository;
    private final AssessmentItemRepository itemRepository;
    private final AssessmentMapper assessmentMapper;

    @Override
    @Transactional
    public CopyStatsResponseDto copyCriteriaFromTemplate(UUID assessmentId, CopyCriteriaRequestDto request) {
        log.info("Starting criteria copy. Assessment: {}, Template: {}", assessmentId, request.getTemplateId());

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> {
                    log.error("Failed to find Assessment with ID: {}", assessmentId);
                    return new ResourceNotFoundException("Assessment not found with ID: " + assessmentId);
                });

        AssessmentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> {
                    log.error("Failed to find Template with ID: {}", request.getTemplateId());
                    return new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId());
                });

        if (template.getStatus() != TemplateStatus.PUBLISHED) {
            log.warn("Attempted to copy from non-published template: {}. Status: {}", template.getId(), template.getStatus());
            throw new BusinessValidationException("Cannot copy from a template that is not PUBLISHED.");
        }

        List<TemplateCriterion> sourceCriteria = template.getCriteria();
        int totalSource = sourceCriteria.size();
        log.debug("Found {} criteria in template", totalSource);

        if (request.getIncludeSections() != null && !request.getIncludeSections().isEmpty()) {
            sourceCriteria = sourceCriteria.stream()
                    .filter(c -> request.getIncludeSections().contains(c.getSection()))
                    .collect(Collectors.toList());
            log.info("Filtered criteria by sections. Remaining items: {}", sourceCriteria.size());
        }

        int filteredOut = totalSource - sourceCriteria.size();

        Map<UUID, AssessmentItem> existingItemsMap = assessment.getItems().stream()
                .filter(item -> item.getCriterionRef() != null)
                .collect(Collectors.toMap(AssessmentItem::getCriterionRef, Function.identity()));

        List<AssessmentItem> itemsToSave = new ArrayList<>();
        int copied = 0;
        int skipped = 0;

        for (TemplateCriterion criterion : sourceCriteria) {
            UUID refId = criterion.getId();

            if (existingItemsMap.containsKey(refId)) {
                if (!request.isOverwriteExisting()) {
                    log.debug("Skipping existing criterion ref: {}", refId);
                    skipped++;
                    continue;
                }
                log.debug("Overwriting existing criterion ref: {}", refId);
                AssessmentItem existingItem = existingItemsMap.get(refId);
                itemsToSave.add(assessmentMapper.updateItemFromCriterion(existingItem, criterion));
            } else {
                AssessmentItem newItem = new AssessmentItem();
                newItem.setAssessment(assessment);
                newItem.setCriterionRef(refId);
                itemsToSave.add(assessmentMapper.updateItemFromCriterion(newItem, criterion));
            }
            copied++;
        }

        itemRepository.saveAll(itemsToSave);
        log.info("Criteria copy finished. Copied: {}, Skipped: {}, Filtered out: {}", copied, skipped, filteredOut);

        return assessmentMapper.toCopyStatsDto(copied, skipped, filteredOut, totalSource);
    }

    @Override
    @Transactional
    public AssessmentDto updateStatus(UUID assessmentId, UpdateStatusRequestDto request) {
        log.info("Updating status for Assessment: {} to {}", assessmentId, request.getStatus());

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));

        AssessmentStatus newStatus = request.getStatus();

        if (newStatus == AssessmentStatus.COMPLETED) {
            boolean hasUnscoredItems = assessment.getItems().stream()
                    .anyMatch(item -> item.getScore() == null);

            if (hasUnscoredItems) {
                log.warn("Validation failed: Cannot complete Assessment {} due to unscored items", assessmentId);
                throw new BusinessValidationException("Cannot complete assessment. All items must have a score.");
            }
        }

        assessment.setStatus(newStatus);
        Assessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Successfully updated Assessment {} status to {}", assessmentId, newStatus);

        return assessmentMapper.toDto(savedAssessment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "assessments", key = "#pageable.pageNumber")
    public Page<AssessmentDto> findAll(Pageable pageable) {
        log.info("Fetching all assessments for page: {}", pageable.getPageNumber());
        Page<Assessment> assessments = assessmentRepository.findAll(pageable);
        return assessmentMapper.toDtoPage(assessments);
    }
}