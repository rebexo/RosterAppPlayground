package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.SchemaTemplateAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchemaTemplateAssignmentRepository extends JpaRepository<SchemaTemplateAssignment, Long> {
    boolean existsByTemplateId(UUID templateId);
}
