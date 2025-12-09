package org.roster.backend.application.port.in;

import org.roster.backend.adapter.in.web.dto.CalculatedShiftDto;
import org.roster.backend.adapter.in.web.dto.SchemaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface iSchemaService {
    SchemaDto createSchema(SchemaDto schemaDto);

    @Transactional(readOnly = true)
    List<SchemaDto> getSchemasForCurrentUser();

    @Transactional(readOnly = true)
    List<CalculatedShiftDto> getSchemaDetails(UUID schemaId);

    SchemaDto updateExpectedEntries(UUID schemaId, Integer count);

    @Transactional(readOnly = true)
    SchemaDto getSchemaById(UUID schemaId);
}
