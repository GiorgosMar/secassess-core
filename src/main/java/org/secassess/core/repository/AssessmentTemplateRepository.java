package org.secassess.core.repository;

import org.secassess.core.model.AssessmentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for AssessmentTemplate entity
 */
public interface AssessmentTemplateRepository extends JpaRepository<AssessmentTemplate, UUID> {
}