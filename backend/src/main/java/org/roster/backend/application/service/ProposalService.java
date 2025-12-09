package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iProposalService;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.ProposalPort;
import org.roster.backend.adapter.in.web.dto.AssignedShiftDto;
import org.roster.backend.adapter.in.web.dto.EmployeeSummaryDto;
import org.roster.backend.adapter.in.web.dto.ProposalDetailDto;
import org.roster.backend.domain.AvailabilityEntry;
import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.domain.Shift;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalService implements iProposalService {

    private final ProposalPort proposalPort;
    private final AvailabilityPort availabilityPort;

    @Override
    public ProposalDetailDto getProposalDetails(UUID proposalId) {
        // 1. Lade den Vorschlag und alle zugehörigen Daten (Schema, Schichten, Basis-Schichten)
        ScheduleProposal proposal = proposalPort.findById(proposalId) // (Diese Custom-Methode musst du im Repo anlegen)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        // 2. Mappe die zugewiesenen Schichten (untere Tabelle im Wireframe)
        List<AssignedShiftDto> assignedShifts = proposal.getProposalShifts().stream()
                .map(ps -> {
                    AssignedShiftDto dto = new AssignedShiftDto();
                    LocalDate shiftDate = ps.getDate(); // Date an dem Schicht anfängt
                    dto.setDate(shiftDate);
                    dto.setShiftName(ps.getBaseShift().getName());
                    dto.setPositionName(ps.getPositionName());
                    dto.setStaffName(ps.getAssignedStaffName());

                    Shift baseShift = ps.getBaseShift();
                    dto.setStartDateTime(shiftDate.atTime(baseShift.getStartTime()));
                    if (baseShift.isOvernightShift()) {
                        dto.setEndDateTime(shiftDate.plusDays(1).atTime(baseShift.getEndTime()));
                    } else {
                        dto.setEndDateTime(shiftDate.atTime(baseShift.getEndTime()));
                    }
                    return dto;
                })
                .sorted(Comparator.comparing(AssignedShiftDto::getDate).thenComparing(AssignedShiftDto::getShiftName))
                .collect(Collectors.toList());

        // 3. Berechne die Mitarbeiter-Zusammenfassung (obere Tabelle im Wireframe)

        // 3a. Hole die Ziel-Schichten aus den AvailabilityEntries
        List<AvailabilityEntry> entries = availabilityPort.findAllBySchemaId(proposal.getSchema().getId());
        Map<String, Integer> targetCounts = entries.stream()
                .collect(Collectors.toMap(AvailabilityEntry::getStaffName, AvailabilityEntry::getTargetShiftCount));

        // 3b. Zähle die tatsächlich zugewiesenen Schichten aus dem Vorschlag
        Map<String, Long> actualCounts = assignedShifts.stream()
                .filter(as -> as.getStaffName() != null)
                .collect(Collectors.groupingBy(AssignedShiftDto::getStaffName, Collectors.counting()));

        // 3c. Kombiniere beide Maps
        List<EmployeeSummaryDto> employeeSummary = targetCounts.keySet().stream()
                .map(staffName -> {
                    EmployeeSummaryDto dto = new EmployeeSummaryDto();
                    dto.setStaffName(staffName);
                    dto.setTargetCount(targetCounts.getOrDefault(staffName, 0));
                    dto.setActualCount(actualCounts.getOrDefault(staffName, 0L).intValue());
                    return dto;
                })
                .sorted(Comparator.comparing(EmployeeSummaryDto::getStaffName))
                .collect(Collectors.toList());

        // 4. Alles zusammenfügen
        ProposalDetailDto detailDto = new ProposalDetailDto();
        detailDto.setSchemaName(proposal.getSchema().getName());
        detailDto.setEmployeeSummary(employeeSummary);
        detailDto.setAssignedShifts(assignedShifts);

        return detailDto;
    }

    /**
     * Ruft die ID des zuletzt generierten Schedule Proposals für ein spezifisches Schema ab.
     * @param schemaId Die ID des Schemas.
     * @return Optional mit der UUID des letzten Proposals für dieses Schema, falls vorhanden.
     */
    @Override
    public Optional<UUID> getLatestProposalIdBySchemaId(UUID schemaId) {
        return proposalPort.findLatestBySchemaId(schemaId)
                .map(ScheduleProposal::getId); //muss hier nicht ScheduleProposal.getSchema.getId passieren?
    }

}


