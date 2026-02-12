package org.secassess.core.service;

import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
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

/**
 * Service implementation for managing assessments, handling the core logic for criteria copying and status transitions.
 */
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
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));

        AssessmentTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + request.getTemplateId()));

        if (template.getStatus() != TemplateStatus.PUBLISHED) {
            throw new BusinessValidationException("Cannot copy from a template that is not PUBLISHED.");
        }

        List<TemplateCriterion> sourceCriteria = template.getCriteria();
        int totalSource = sourceCriteria.size();

        if (request.getIncludeSections() != null && !request.getIncludeSections().isEmpty()) {
            sourceCriteria = sourceCriteria.stream()
                    .filter(c -> request.getIncludeSections().contains(c.getSection()))
                    .collect(Collectors.toList());
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
                    skipped++;
                    continue;
                }
                AssessmentItem existingItem = existingItemsMap.get(refId);

                AssessmentItem updatedItem = assessmentMapper.updateItemFromCriterion(existingItem, criterion);
                itemsToSave.add(updatedItem);
                copied++;
            } else {
                AssessmentItem newItem = new AssessmentItem();
                newItem.setAssessment(assessment);
                newItem.setCriterionRef(refId);

                AssessmentItem preparedItem = assessmentMapper.updateItemFromCriterion(newItem, criterion);
                itemsToSave.add(preparedItem);
                copied++;
            }
        }

        itemRepository.saveAll(itemsToSave);

        return assessmentMapper.toCopyStatsDto(copied, skipped, filteredOut, totalSource);
    }

    @Override
    @Transactional
    public AssessmentDto updateStatus(UUID assessmentId, UpdateStatusRequestDto request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));

        AssessmentStatus newStatus = request.getStatus();

        if (newStatus == AssessmentStatus.COMPLETED) {
            boolean hasUnscoredItems = assessment.getItems().stream()
                    .anyMatch(item -> item.getScore() == null);

            if (hasUnscoredItems) {
                throw new BusinessValidationException("Cannot complete assessment. All items must have a score.");
            }
        }

        assessment.setStatus(newStatus);
        Assessment savedAssessment = assessmentRepository.save(assessment);

        return assessmentMapper.toDto(savedAssessment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "assessments", key = "#pageable.pageNumber")
    public Page<AssessmentDto> findAll(Pageable pageable) {
        Page<Assessment> assessments = assessmentRepository.findAll(pageable);
        return assessmentMapper.toDtoPage(assessments);
    }
}