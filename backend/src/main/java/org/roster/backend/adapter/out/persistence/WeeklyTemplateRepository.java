package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WeeklyTemplateRepository extends JpaRepository<WeeklyTemplate, UUID> {
    // Prüft, ob ein Template mit diesem Namen für den User bereits existiert (außer dem aktuellen)
    boolean existsByNameAndPlannerAndIdNot(String name, User planner, UUID id);
}