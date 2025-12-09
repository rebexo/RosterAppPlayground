package org.roster.backend.application.port.out;

import org.roster.backend.domain.ScheduleSchema;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchemaPort {

    /**
     * Speichert ein Schema (Create oder Update).
     * Aufgrund von CascadeType.ALL in der Entity werden dabei auch
     * die TemplateAssignments aktualisiert/gespeichert.
     */
    ScheduleSchema saveSchema(ScheduleSchema schema);

    /**
     * Lädt alle Schemata.
     * (Wird aktuell im Service gefiltert, später könnte man hier
     * findSchemasByPlanner(UUID plannerId) ergänzen).
     */
    List<ScheduleSchema> findAllSchemas();

    /**
     * Findet ein Schema anhand seiner UUID.
     */
    Optional<ScheduleSchema> findSchemaById(UUID id);

    /**
     * Findet ein Schema anhand des öffentlichen Verfügbarkeits-Links.
     * Wichtig für den AvailabilityService (US-10).
     */
    Optional<ScheduleSchema> findSchemaByLinkID(String linkID);

    /**
     * Löscht ein Schema.
     */
    void deleteSchema(ScheduleSchema schema);
}