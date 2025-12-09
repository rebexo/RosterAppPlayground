package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iSchemaService;
import org.roster.backend.application.port.out.SchemaPort;
import org.roster.backend.application.port.out.TemplatePort;
import org.roster.backend.adapter.in.web.dto.SchemaDto;
import org.roster.backend.adapter.in.web.dto.SchemaTemplateAssignmentDto;
import org.roster.backend.adapter.in.web.dto.CalculatedShiftDto;
import org.roster.backend.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional
public class SchemaService implements iSchemaService {

    private final SchemaPort schemaPort;
    private final TemplatePort templatePort;

    @Override
    public SchemaDto createSchema(SchemaDto schemaDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 1. Haupt-Entität erstellen und Grunddaten setzen
        ScheduleSchema newSchema = new ScheduleSchema();
        newSchema.setName(schemaDto.getName());
        newSchema.setStartDate(schemaDto.getStartDate());
        newSchema.setEndDate(schemaDto.getEndDate());
        newSchema.setPlanner(currentUser);

        // 2. Einzigartigen, schwer zu erratenden Link-Token generieren
        newSchema.setAvailabilityLinkID(UUID.randomUUID().toString().replace("-", ""));

        // 3. Template-Zuweisungen verarbeiten
        List<SchemaTemplateAssignment> assignments = schemaDto.getTemplateAssignments().stream()
                .map(dto -> {
                    // Finde das zugehörige Template in der DB
                    WeeklyTemplate template = templatePort.findTemplateById(dto.getTemplateId())
                            .orElseThrow(() -> new IllegalArgumentException("Template nicht gefunden"));

                    // Erstelle die Zuweisungs-Entität
                    SchemaTemplateAssignment assignment = new SchemaTemplateAssignment();
                    assignment.setValidFrom(dto.getValidFrom());
                    assignment.setValidTo(dto.getValidTo());
                    assignment.setTemplate(template);
                    assignment.setSchema(newSchema); // Rückverweis auf das Schema
                    return assignment;
                }).collect(Collectors.toList());

        newSchema.setTemplateAssignments(assignments);

        // 4. Alles speichern (Dank CascadeType.ALL)
        ScheduleSchema savedSchema = schemaPort.saveSchema(newSchema);

        return mapToDto(savedSchema);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SchemaDto> getSchemasForCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO Port methode findAllByPlanner(UUID id) geben
        return schemaPort.findAllSchemas().stream()
                .filter(schema -> schema.getPlanner().getId().equals(currentUser.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<CalculatedShiftDto> getSchemaDetails(UUID schemaId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
                .orElseThrow(() -> new RuntimeException("Schema nicht gefunden"));

        if (!schema.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Zugriff verweigert.");
        }

        List<CalculatedShiftDto> calculatedShifts = new ArrayList<>();

        // Iteriere durch jeden Tag im Zeitraum des Schemas
        for (LocalDate date = schema.getStartDate(); !date.isAfter(schema.getEndDate()); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            // Finde das passende Template für den aktuellen Tag
            schema.getTemplateAssignments().stream()
                    .filter(assignment -> !currentDate.isBefore(assignment.getValidFrom()) && !currentDate.isAfter(assignment.getValidTo()))
                    .findFirst()
                    .ifPresent(activeAssignment -> {
                        WeeklyTemplate template = activeAssignment.getTemplate();
                        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek(); // MONDAY, TUESDAY, ...
                        int weekdayIndex = currentDayOfWeek.getValue() - 1; // Konvertiere zu 0=Mo, 1=Di, ...

                        // Finde alle Schicht-Zuweisungen im Template für diesen Wochentag
                        template.getShifts().stream()
                                .filter(templateShift -> templateShift.getWeekday() == weekdayIndex)
                                .forEach(ts -> {
                                    Shift baseShift = ts.getShift();
                                    CalculatedShiftDto dto = new CalculatedShiftDto();
                                    dto.setBaseShiftId(baseShift.getId());
                                    dto.setTitle(ts.getShift().getName() + " (" + ts.getPositionName() + ")");
                                    dto.setStartDateTime(currentDate.atTime(baseShift.getStartTime()));
                                    if (baseShift.isOvernightShift()) {
                                        dto.setEndDateTime(currentDate.plusDays(1).atTime(baseShift.getEndTime()));
                                    } else {
                                        dto.setEndDateTime(currentDate.atTime(baseShift.getEndTime()));
                                    }
                                    calculatedShifts.add(dto);
                                });
                    });
        }

        // Sortiere die Schichten nach Datum und Startzeit
        calculatedShifts.sort(Comparator.comparing(CalculatedShiftDto::getStartDateTime));

        return calculatedShifts;
    }

    @Override
    public SchemaDto updateExpectedEntries(UUID schemaId, Integer count) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
                .orElseThrow(() -> new RuntimeException("Schema nicht gefunden"));

        if (!schema.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Zugriff verweigert.");
        }

        schema.setExpectedEntries(count);
        ScheduleSchema savedSchema = schemaPort.saveSchema(schema);
        return mapToDto(savedSchema);
    }

    @Transactional(readOnly = true)
    @Override
    public SchemaDto getSchemaById(UUID schemaId) {
        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
                .orElseThrow(() -> new IllegalArgumentException("ScheduleSchema not found with ID: " + schemaId));
        return mapToDto(schema);
    }

    // Hilfsmethode zur Konvertierung
    private SchemaDto mapToDto(ScheduleSchema schema) {
        SchemaDto dto = new SchemaDto();
        dto.setId(schema.getId());
        dto.setName(schema.getName());
        dto.setStartDate(schema.getStartDate());
        dto.setEndDate(schema.getEndDate());
        dto.setExpectedEntries(schema.getExpectedEntries());
        dto.setSubmittedEntriesCount(schema.getSubmittedEntriesCount());
        dto.setAvailabilityLinkID(schema.getAvailabilityLinkID());
        dto.setCollectionClosed(schema.isCollectionClosed());

        List<SchemaTemplateAssignmentDto> assignmentDtos = schema.getTemplateAssignments().stream()
                .map(a -> {
                    SchemaTemplateAssignmentDto aDto = new SchemaTemplateAssignmentDto();
                    aDto.setTemplateId(a.getTemplate().getId());
                    aDto.setValidFrom(a.getValidFrom());
                    aDto.setValidTo(a.getValidTo());
                    return aDto;
                }).collect(Collectors.toList());
        dto.setTemplateAssignments(assignmentDtos);

        return dto;
    }
}