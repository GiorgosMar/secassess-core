package org.secassess.core.repository;

import org.secassess.core.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Project entity
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}