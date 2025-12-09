package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.ScheduleSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleSchemaRepository extends JpaRepository<ScheduleSchema, UUID> {

    // Option A: Mit @EntityGraph (oft einfacher zu handhaben)
    // Definiert, welche Assoziationen EAGER geladen werden sollen.
    // EntityGraph funktioniert, indem es die Joins für die angegebenen Pfade generiert.
//    @EntityGraph(attributePaths = {
//            "templateAssignments",
//            "templateAssignments.template", // Lädt WeeklyTemplate
//            "templateAssignments.template.shifts", // Lädt TemplateShifts innerhalb des WeeklyTemplate
//            "templateAssignments.template.shifts.shift" // Lädt die eigentliche backend.domain.Shift aus dem TemplateShift
//    })
//    Optional<ScheduleSchema> findByIdWithFullTemplates(@Param("id") UUID id);

    Optional<ScheduleSchema> findByAvailabilityLinkID(String availabilityLinkID);
}
