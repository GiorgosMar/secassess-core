package org.secassess.core.repository;

import org.secassess.core.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repository for Assessment entity
 */
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
}