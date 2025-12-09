package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.ScheduleProposal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleProposalRepository extends JpaRepository<ScheduleProposal, UUID> {

    @EntityGraph(attributePaths = {
            "schema",                      // Lädt das zugehörige ScheduleSchema
            "proposalShifts",              // Lädt die Liste der ProposalShifts
            "proposalShifts.baseShift"     // Lädt für jede ProposalShift deren BaseShift
    })
    Optional<ScheduleProposal> findById(UUID id);

    // Holen des letzten generierten ScheduleProposal für ein bestimmtes Schema, basierend auf generatedAt
    // (Annahme: ScheduleProposal hat ein field 'creationDateTime')
    @Query("SELECT sp FROM ScheduleProposal sp WHERE sp.schema.id = :schemaId ORDER BY sp.generatedAt DESC LIMIT 1")
    Optional<ScheduleProposal> findLatestProposalBySchemaId(UUID schemaId);

}