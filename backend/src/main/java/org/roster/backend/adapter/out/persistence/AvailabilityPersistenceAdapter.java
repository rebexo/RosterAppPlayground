package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.domain.AvailabilityEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvailabilityPersistenceAdapter implements AvailabilityPort {

    private final AvailabilityEntryRepository availabilityEntryRepository;

    @Override
    public AvailabilityEntry save(AvailabilityEntry entry) {
        return availabilityEntryRepository.save(entry);
    }

    @Override
    public Optional<AvailabilityEntry> findById(UUID id) {
        return availabilityEntryRepository.findById(id);
    }

    @Override
    public List<AvailabilityEntry> findAllBySchemaId(UUID schemaId) {
        return availabilityEntryRepository.findAllBySchemaId(schemaId);
    }
}
