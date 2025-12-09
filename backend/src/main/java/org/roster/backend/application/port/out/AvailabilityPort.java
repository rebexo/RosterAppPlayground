package org.roster.backend.application.port.out;

import org.roster.backend.domain.AvailabilityEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityPort {

    /**
     * Speichert einen Verfügbarkeitseintrag (Create oder Update).
     * Dank CascadeType.ALL werden auch die AvailabilityDetails gespeichert.
     */
    AvailabilityEntry save(AvailabilityEntry entry);

    /**
     * Findet einen Eintrag anhand seiner ID.
     */
    Optional<AvailabilityEntry> findById(UUID id);

    /**
     * Findet alle Einträge, die zu einem bestimmten Schema gehören.
     * Sollte idealerweise EAGER loading für die Details nutzen.
     */
    List<AvailabilityEntry> findAllBySchemaId(UUID schemaId);

    // Optional: Löschen, falls benötigt
    // void delete(AvailabilityEntry entry);
}