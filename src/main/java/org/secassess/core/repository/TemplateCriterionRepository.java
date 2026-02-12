package org.secassess.core.repository;

import org.secassess.core.model.TemplateCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for TemplateCriterion entity
 */
public interface TemplateCriterionRepository extends JpaRepository<TemplateCriterion, UUID> {
}