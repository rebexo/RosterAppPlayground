package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.TemplatePort;
import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemplatePersistenceAdapter implements TemplatePort {

    private final WeeklyTemplateRepository weeklyTemplateRepository;
    private final SchemaTemplateAssignmentRepository schemaTemplateAssignmentRepository;

    @Override
    public WeeklyTemplate saveTemplate(WeeklyTemplate template) {
        return weeklyTemplateRepository.save(template);
    }

    @Override
    public List<WeeklyTemplate> findAllTemplates() {
        return weeklyTemplateRepository.findAll();
    }

    @Override
    public Optional<WeeklyTemplate> findTemplateById(UUID id) {
        return weeklyTemplateRepository.findById(id);
    }

    @Override
    public void deleteTemplate(WeeklyTemplate template) {
        weeklyTemplateRepository.delete(template);
    }

    @Override
    public boolean existsByNameAndPlanner(String name, User planner, UUID excludeId) {
        return weeklyTemplateRepository.existsByNameAndPlannerAndIdNot(name, planner, excludeId);
    }

    @Override
    public boolean isTemplateUsedInSchema(UUID templateId) {
        return schemaTemplateAssignmentRepository.existsByTemplateId(templateId);
    }
}
