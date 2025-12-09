package org.roster.backend.application.port.out;

import org.roster.backend.domain.ScheduleProposal;
import java.util.Optional;
import java.util.UUID;

public interface ProposalPort {

    /**
     * Speichert einen generierten Dienstplan-Vorschlag.
     * Dank CascadeType.ALL werden auch die ProposalShifts gespeichert.
     */
    ScheduleProposal save(ScheduleProposal proposal);

    /**
     * Findet einen Vorschlag anhand seiner ID.
     * Sollte idealerweise ProposalShifts und BaseShifts mitladen (EAGER/EntityGraph).
     */
    Optional<ScheduleProposal> findById(UUID id);

    /**
     * Findet den zuletzt generierten Vorschlag für ein bestimmtes Schema.
     */
    Optional<ScheduleProposal> findLatestBySchemaId(UUID schemaId);
}