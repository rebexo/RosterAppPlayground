package org.roster.backend.adapter.out.solver;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.application.port.out.SolverPort;
import org.roster.backend.domain.AvailabilityEntry;
import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.domain.ScheduleProposalShift;
import org.roster.backend.domain.ScheduleSchema;
import org.roster.backend.domain.WeeklyTemplate;
import org.roster.backend.domain.enums.ProposalStatus;
import org.roster.backend.solver.domain.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimefoldSolverAdapter implements SolverPort {

    private final SolverManager<RosterSolution, UUID> solverManager;
    private final AvailabilityPort availabilityPort;
    private final ShiftPort shiftPort;

    @Override
    public ScheduleProposal solve(ScheduleSchema schema) {
        log.info("Starting solver adapter for schema: {}", schema.getId());

        // 1. Daten laden & Mapping (Domain -> Solver)
        RosterSolution problem = mapToSolverProblem(schema);

        // 2. Lösen (Timefold)
        UUID problemId = schema.getId();
        SolverJob<RosterSolution, UUID> solverJob = solverManager.solve(problemId, problem);

        RosterSolution solution;
        try {
            // Blockierender Aufruf (in Produktion evtl. asynchron handhaben)
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Solving failed in Timefold adapter.", e);
        }

        // 3. Mapping zurück (Solver -> Domain)
        return mapToDomain(schema, solution);
    }

    // --- MAPPING LOGIC (Domain -> Solver) ---

    private RosterSolution mapToSolverProblem(ScheduleSchema schema) {
        // Availabilities laden
        List<AvailabilityEntry> entries = availabilityPort.findAllBySchemaId(schema.getId());

        // Employees mappen
        List<Employee> employees = entries.stream()
                .map(e -> new Employee(e.getId(), e.getStaffName(), e.getTargetShiftCount()))
                .collect(Collectors.toList());

        // Availabilities mappen
        List<Availability> availabilities = entries.stream()
                .flatMap(e -> e.getDetails().stream()
                        .map(d -> new Availability(e.getId(), d.getDate(), d.getStatus())))
                .collect(Collectors.toList());

        // Shifts generieren
        List<Shift> concreteShifts = generateConcreteShifts(schema);

        // Initiale (leere) Zuweisungen erstellen
        List<ShiftAssignment> assignments = concreteShifts.stream()
                .map(s -> new ShiftAssignment(UUID.randomUUID(), s, null))
                .collect(Collectors.toList());

        return new RosterSolution(employees, concreteShifts, availabilities, assignments);
    }

    private List<Shift> generateConcreteShifts(ScheduleSchema schema) {
        List<Shift> concreteShifts = new ArrayList<>();

        for (LocalDate date = schema.getStartDate(); !date.isAfter(schema.getEndDate()); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            schema.getTemplateAssignments().stream()
                    .filter(assignment -> !currentDate.isBefore(assignment.getValidFrom()) && !currentDate.isAfter(assignment.getValidTo()))
                    .findFirst()
                    .ifPresent(activeAssignment -> {
                        WeeklyTemplate template = activeAssignment.getTemplate();
                        int weekdayIndex = currentDate.getDayOfWeek().getValue() - 1;

                        // Achtung: template.getShifts() muss geladen sein (EAGER oder Transactional im Service)
                        template.getShifts().stream()
                                .filter(ts -> ts.getWeekday() == weekdayIndex)
                                .forEach(ts -> {
                                    Shift solverShift = new Shift(
                                            UUID.randomUUID(),
                                            ts.getShift().getName() + " (" + ts.getPositionName() + ")",
                                            currentDate,
                                            ts.getShift().getStartTime(),
                                            ts.getShift().getEndTime(),
                                            ts.getShift().getId() // Base Shift ID für Mapping zurück
                                    );
                                    concreteShifts.add(solverShift);
                                });
                    });
        }
        return concreteShifts;
    }


    // --- MAPPING LOGIC (Solver -> Domain) ---

    private ScheduleProposal mapToDomain(ScheduleSchema schema, RosterSolution solution) {
        ScheduleProposal proposal = new ScheduleProposal();
        proposal.setSchema(schema);
        proposal.setGeneratedAt(java.time.LocalDateTime.now());

        // Status setzen
        boolean isFeasible = solution.getScore() != null && solution.getScore().getHardScore() >= 0;
        proposal.setStatus(isFeasible ? ProposalStatus.COMPLETED : ProposalStatus.FAILED);

        List<ScheduleProposalShift> proposalShifts = solution.getShiftAssignments().stream()
                .map(assignment -> mapAssignmentToProposalShift(assignment, proposal))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        proposal.setProposalShifts(proposalShifts);
        return proposal;
    }

    private ScheduleProposalShift mapAssignmentToProposalShift(ShiftAssignment assignment, ScheduleProposal proposal) {
        if (assignment.getEmployee() == null) {
            return null; // Unzugewiesene Schicht
        }

        UUID baseShiftId = assignment.getShift().getBaseShiftId();
        org.roster.backend.domain.Shift baseShift = shiftPort.findShiftById(baseShiftId)
                .orElseThrow(() -> new IllegalStateException("Base shift not found for ID: " + baseShiftId));

        ScheduleProposalShift proposalShift = new ScheduleProposalShift();
        proposalShift.setProposal(proposal); // Bidirektionale Beziehung setzen
        proposalShift.setBaseShift(baseShift);
        proposalShift.setDate(assignment.getShift().getStartDateTime().toLocalDate());
        proposalShift.setPositionName(extractPositionName(assignment.getShift().getName()));
        proposalShift.setAssignedStaffName(assignment.getEmployee().getName());

        return proposalShift;
    }

    private String extractPositionName(String solverShiftName) {
        if (solverShiftName == null) return null;
        int openParen = solverShiftName.lastIndexOf('(');
        int closeParen = solverShiftName.lastIndexOf(')');
        if (openParen != -1 && closeParen != -1 && openParen < closeParen) {
            return solverShiftName.substring(openParen + 1, closeParen).trim();
        }
        return null;
    }
}