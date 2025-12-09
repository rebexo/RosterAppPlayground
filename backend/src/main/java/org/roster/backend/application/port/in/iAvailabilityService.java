package org.roster.backend.application.port.in;

import org.roster.backend.adapter.in.web.dto.AvailabilityEntryDetailDto;
import org.roster.backend.adapter.in.web.dto.NewAvailabilityEntryDto;
import org.roster.backend.adapter.in.web.dto.PublicSchemaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface iAvailabilityService {
    @Transactional(readOnly = true)
    PublicSchemaDto getPublicSchema(String linkId);

    void submitAvailability(String linkId, NewAvailabilityEntryDto dto);

    // Nur Lesezugriff
    @Transactional(readOnly = true)
    List<AvailabilityEntryDetailDto> getAvailabilityEntriesDetailsForSchema(UUID schemaId);

    // Schreibzugriff
    @Transactional
    void updateAvailabilityEntryTargetShiftCount(UUID entryId, Integer targetShiftCount);
}
