package org.secassess.core.repository;

import org.secassess.core.model.AssessmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for AssessmentItem entity
 */
public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, UUID> {
}