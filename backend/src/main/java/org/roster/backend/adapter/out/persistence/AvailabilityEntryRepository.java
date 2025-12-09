package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.AvailabilityEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AvailabilityEntryRepository extends JpaRepository<AvailabilityEntry, UUID> {
    // Diese Methode lädt die 'details' Liste von AvailabilityEntrys eager (JOIN FETCH).
    // Dies ist entscheidend, um LazyInitializationException zu vermeiden, wenn du auf entry.getDetails() zugreifst.
    @EntityGraph(attributePaths = "details")
    List<AvailabilityEntry> findAllBySchemaId(@Param("schemaId") UUID schemaId);

    List<AvailabilityEntry> findBySchemaId(UUID schemaId);
}

