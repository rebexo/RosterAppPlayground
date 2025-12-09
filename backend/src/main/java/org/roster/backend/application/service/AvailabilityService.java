package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iAvailabilityService;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.SchemaPort;
import org.roster.backend.adapter.in.web.dto.NewAvailabilityEntryDto;
import org.roster.backend.adapter.in.web.dto.PublicSchemaDto;
import org.roster.backend.adapter.in.web.dto.AvailabilityEntryDetailDto;
import org.roster.backend.domain.AvailabilityDetail;
import org.roster.backend.domain.AvailabilityEntry;
import org.roster.backend.domain.ScheduleSchema;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityService implements iAvailabilityService {

    private final SchemaPort schemaPort;
    private final AvailabilityPort availabilityPort;

    @Transactional(readOnly = true)
    @Override
    public PublicSchemaDto getPublicSchema(String linkId) {
        ScheduleSchema schema = findAndValidateSchema(linkId);

        PublicSchemaDto dto = new PublicSchemaDto();
        dto.setName(schema.getName());
        dto.setStartDate(schema.getStartDate());
        dto.setEndDate(schema.getEndDate());
        return dto;
    }

    @Override
    public void submitAvailability(String linkId, NewAvailabilityEntryDto dto) {
        ScheduleSchema schema = findAndValidateSchema(linkId);

        AvailabilityEntry entry = new AvailabilityEntry();
        entry.setStaffName(dto.getStaffName());
        entry.setComment(dto.getComment());
        entry.setTargetShiftCount(dto.getTargetShiftCount());
        entry.setSubmittedAt(LocalDateTime.now());
        entry.setSchema(schema);

        List<AvailabilityDetail> details = dto.getDetails().stream().map(detailDto -> {
            AvailabilityDetail detail = new AvailabilityDetail();
            detail.setDate(detailDto.getDate());
            detail.setStatus(detailDto.getStatus());
            detail.setEntry(entry);
            return detail;
        }).collect(Collectors.toList());

        entry.setDetails(details);

        availabilityPort.save(entry);
    }

    private ScheduleSchema findAndValidateSchema(String linkId) {
        ScheduleSchema schema = schemaPort.findSchemaByLinkID(linkId)
                .orElseThrow(() -> new RuntimeException("Schema nicht gefunden. Link ist ungültig."));

        if (schema.isCollectionClosed()) {
            throw new RuntimeException("Die Eintragung für diesen Dienstplan ist bereits geschlossen.");
        }
        return schema;
    }

    @Transactional(readOnly = true)
    @Override
    public List<AvailabilityEntryDetailDto> getAvailabilityEntriesDetailsForSchema(UUID schemaId) {
        List<AvailabilityEntry> entries = availabilityPort.findAllBySchemaId(schemaId);

        return entries.stream()
                .map(entry -> {
                    List<LocalDate> unavailableDates = entry.getDetails().stream()
                            .filter(detail -> detail.getStatus() == AvailabilityStatus.UNAVAILABLE)
                            .map(AvailabilityDetail::getDate) // Kürzer als detail -> detail.getDate()
                            .collect(Collectors.toList());

                    // ggf preferredDates ebenfalls extrahieren, falls im DTO vorhanden
                    // List<LocalDate> preferredDates = entry.getDetails().stream()
                    //        .filter(detail -> detail.getStatus() == AvailabilityStatus.PREFERRED)
                    //        .map(AvailabilityDetail::getDate)
                    //        .collect(Collectors.toList());

                    return new AvailabilityEntryDetailDto(entry.getId(), entry.getStaffName(), entry.getTargetShiftCount(), unavailableDates /*, preferredDates*/);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void updateAvailabilityEntryTargetShiftCount(UUID entryId, Integer targetShiftCount) {
        AvailabilityEntry entry = availabilityPort.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("AvailabilityEntry not found with ID: " + entryId));

        entry.setTargetShiftCount(targetShiftCount);
        availabilityPort.save(entry);
    }



}