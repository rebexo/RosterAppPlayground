package org.roster.backend.application.port.out;

import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplatePort {

    /**
     * Speichert ein Template (Create oder Update).
     */
    WeeklyTemplate saveTemplate(WeeklyTemplate template);

    /**
     * Lädt alle Templates.
     * (Optimierung: Könnte auch findAllByPlanner sein, um DB-Last zu senken)
     */
    List<WeeklyTemplate> findAllTemplates();

    /**
     * Findet ein Template anhand der ID.
     */
    Optional<WeeklyTemplate> findTemplateById(UUID id);

    /**
     * Löscht ein Template.
     */
    void deleteTemplate(WeeklyTemplate template);

    /**
     * Prüft, ob der Name für diesen Planer schon vergeben ist (Business Rule: Eindeutige Namen).
     * @param excludeId ID des aktuellen Templates (bei Updates), sonst null.
     */
    boolean existsByNameAndPlanner(String name, User planner, UUID excludeId);

    /**
     * Prüft, ob das Template in einem Dienstplan (Schema) verwendet wird.
     * Ersetzt den direkten Zugriff auf SchemaTemplateAssignmentRepository.
     */
    boolean isTemplateUsedInSchema(UUID templateId);
}