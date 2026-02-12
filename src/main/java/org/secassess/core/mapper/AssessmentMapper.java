package org.secassess.core.mapper;

import org.secassess.core.dto.AssessmentDto;
import org.secassess.core.dto.AssessmentItemDto;
import org.secassess.core.dto.CopyStatsResponseDto;
import org.secassess.core.model.Assessment;
import org.secassess.core.model.AssessmentItem;
import org.secassess.core.model.TemplateCriterion;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A dedicated component for clean mapping between database entities and DTOs
 */
@Component
public class AssessmentMapper {

    public AssessmentDto toDto(Assessment entity) {
        if (entity == null) return null;

        List<AssessmentItemDto> itemDtos = entity.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return AssessmentDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(itemDtos)
                .build();
    }

    private AssessmentItemDto toItemDto(AssessmentItem item) {
        return AssessmentItemDto.builder()
                .id(item.getId())
                .criterionRef(item.getCriterionRef())
                .section(item.getSection())
                .text(item.getText())
                .severity(item.getSeverity())
                .weight(item.getWeight())
                .score(item.getScore())
                .notes(item.getNotes())
                .build();
    }

    public Page<AssessmentDto> toDtoPage(Page<Assessment> page) {
        return page.map(this::toDto);
    }

    public AssessmentItem updateItemFromCriterion(AssessmentItem target, TemplateCriterion source) {
        target.setSection(source.getSection());
        target.setText(source.getText());
        target.setSeverity(source.getSeverity());
        target.setWeight(source.getWeight());

        return target;
    }

    public CopyStatsResponseDto toCopyStatsDto(int copied, int skipped, int filtered, int total) {
        return CopyStatsResponseDto.builder()
                .copied(copied)
                .skippedDuplicates(skipped)
                .filteredOut(filtered)
                .totalSource(total)
                .build();
    }
}