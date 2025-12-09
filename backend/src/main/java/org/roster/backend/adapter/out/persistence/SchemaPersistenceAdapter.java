package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.SchemaPort;
import org.roster.backend.domain.ScheduleSchema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SchemaPersistenceAdapter implements SchemaPort {

    private final ScheduleSchemaRepository scheduleSchemaRepository;

    @Override
    public ScheduleSchema saveSchema(ScheduleSchema schema) {
        return scheduleSchemaRepository.save(schema);
    }

    @Override
    public List<ScheduleSchema> findAllSchemas() {
        return scheduleSchemaRepository.findAll();
    }

    @Override
    public Optional<ScheduleSchema> findSchemaById(UUID id) {
        return scheduleSchemaRepository.findById(id);
    }

    @Override
    public Optional<ScheduleSchema> findSchemaByLinkID(String linkID) {
        return scheduleSchemaRepository.findByAvailabilityLinkID(linkID);
    }

    @Override
    public void deleteSchema(ScheduleSchema schema) {
        scheduleSchemaRepository.delete(schema);
    }
}
