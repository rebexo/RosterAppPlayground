package org.roster.backend.application.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iSolverService;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.ProposalPort;
import org.roster.backend.application.port.out.SchemaPort;
import org.roster.backend.application.port.out.SolverPort;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.domain.*; // normale Entities
import org.roster.backend.domain.enums.ProposalStatus;
import org.roster.backend.solver.domain.*; // Die Solver-Entities
//import org.roster.backend.repository.*; // Repositories
import org.roster.backend.solver.domain.Shift;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*; // Für Set
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolverService implements iSolverService {

    private final SolverPort solverPort;      // Nutzt jetzt den Adapter
    private final SchemaPort schemaPort;
    private final ProposalPort proposalPort;  // Speichert das Ergebnis

    @Transactional
    @Override
    public ScheduleProposal solve(UUID schemaId) {
        // 1. Schema laden
        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
                .orElseThrow(() -> new IllegalArgumentException("Schema not found"));

        // 2. Adapter aufrufen (Rechnen lassen)
        // Der Adapter liefert ein fertig gebautes, aber noch nicht gespeichertes Proposal
        ScheduleProposal proposal = solverPort.solve(schema);

        // 3. Ergebnis speichern
        return proposalPort.save(proposal);
    }
}

//@Service
//@RequiredArgsConstructor
//public class SolverService implements iSolverService {
//
//    private final SolverManager<RosterSolution, UUID> solverManager;
//    private final SchemaPort schemaPort;
//    private final AvailabilityPort availabilityPort;
//    private final ShiftPort shiftPort;
//    private final ProposalPort proposalPort;
//
//    /**
//     * Solves a scheduling problem for the given schema by fetching the necessary data, translating it
//     * into the solver's domain model, and processing it using the solver engine. The method waits for
//     * the solver's result and returns a schedule proposal.
//     *
//     * @param schemaId the unique identifier of the schedule schema for which the scheduling problem
//     *                 is to be solved
//     * @return the generated schedule proposal based on the solver's result
//     * @throws IllegalArgumentException if the provided schemaId does not correspond to an existing
//     *                                  schedule schema
//     * @throws IllegalStateException if the solving process fails due to an unexpected error
//     * @throws InterruptedException or ExecutionException if there are issues during the
//     *                              asynchronous solving process
//     */
//    @Transactional
//    @Override
//    public ScheduleProposal solve(UUID schemaId) {
//        // 1. Lade alle notwendigen Daten aus der Datenbank
//        ScheduleSchema schema = schemaPort.findSchemaById(schemaId)
//                .orElseThrow(() -> new IllegalArgumentException("ScheduleSchema not found with ID: " + schemaId));
//
//        // Laden aller AvailabilityEntries, die zum Schema gehören
//        List<AvailabilityEntry> backendAvailabilityEntries = availabilityPort.findAllBySchemaId(schemaId);
//
//        // 2. Übersetze die DB-Daten in das Solver-Domänenmodell
//        List<Employee> employees = backendAvailabilityEntries.stream()
//                .map(e -> new Employee(e.getId(), e.getStaffName(), e.getTargetShiftCount()))
//                .collect(Collectors.toList());
//        // Erzeugung der konkreten Shifts für den Solver
//        List<Shift> concreteShifts = generateConcreteShifts(schema);
//
//        // Umwandlung der AvailabilityEntryDetails in solver.domain.Availability
//        List<Availability> availabilities = backendAvailabilityEntries.stream()
//                .flatMap(e -> e.getDetails().stream()
//                        .map(d -> new Availability(e.getId(), d.getDate(), d.getStatus())))
//                .collect(Collectors.toList());
//
//        // Erstelle die initialen (unzugewiesenen) Schichtzuweisungen
//        List<ShiftAssignment> assignments = concreteShifts.stream()
//                .map(s -> new ShiftAssignment(UUID.randomUUID(), s, null)) // Gib jeder Zuweisung eine ID
//                .collect(Collectors.toList());
//
//        // 3. Erstelle das Problem-Objekt
//        RosterSolution problem = new RosterSolution(employees, concreteShifts, availabilities, assignments);
//        UUID problemId = schemaId; // verwende Schema-ID als Problem-ID
//
//        // 4. Starte den Solver (asynchron)
//        SolverJob<RosterSolution, UUID> solverJob = solverManager.solve(problemId, problem);
//
//        // 5. Warte auf das Ergebnis und gib es zurück
//        RosterSolution solution;
//        try {
//            solution = solverJob.getFinalBestSolution();
//
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            throw new IllegalStateException("Solving failed.", e);
//        }
//        return createScheduleProposalFromSolution(schema, solution);
//    }
//
//    /**
//     * Extracts the position name enclosed within parentheses from the given solver shift name.
//     * If the input string contains a pair of parentheses, the method returns the content within
//     * the last pair of parentheses. If the parentheses are not present or are improperly formatted,
//     * the method returns null.
//     *
//     * @param solverShiftName the full solver shift name as a string, which may include a position name in parentheses
//     * @return the position name extracted from within the parentheses, or null if no valid parentheses are found or the input is null
//     */
//    private String extractPositionName(String solverShiftName) {
//        if (solverShiftName == null) {
//            return null;
//        }
//        int openParen = solverShiftName.lastIndexOf('(');
//        int closeParen = solverShiftName.lastIndexOf(')');
//        if (openParen != -1 && closeParen != -1 && openParen < closeParen) {
//            return solverShiftName.substring(openParen + 1, closeParen).trim();
//        }
//        return null;
//    }
//
//    /**
//     * Generates a list of concrete Shift instances for the solver based on a ScheduleSchema.
//     * It iterates through the schema's date range and applies the correct template for each day.
//     *
//     * @param schema The ScheduleSchema containing date range and template assignments.
//     * @return A list of Shift objects ready for the Timefold solver.
//     */
//    private List<Shift> generateConcreteShifts(ScheduleSchema schema) {
//        List<Shift> concreteShifts = new ArrayList<>();
//
//        // Iterate through each day within the schema's date range
//        for (LocalDate date = schema.getStartDate(); !date.isAfter(schema.getEndDate()); date = date.plusDays(1)) {
//            final LocalDate currentDate = date; // Final variable for use in lambda expression
//
//            // Find the template assignment valid for the current date
//            schema.getTemplateAssignments().stream()
//                    .filter(assignment -> !currentDate.isBefore(assignment.getValidFrom()) && !currentDate.isAfter(assignment.getValidTo()))
//                    .findFirst() // Assumes non-overlapping assignments, takes the first match
//                    .ifPresent(activeAssignment -> {
//                        WeeklyTemplate template = activeAssignment.getTemplate();
//                        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
//                        int weekdayIndex = currentDayOfWeek.getValue() - 1; // Convert DayOfWeek (1-7) to index (0-6)
//
//                        //TODO
//                        // --- KRITISCHER PUNKT: LAZY LOADING ---
//                        // Sicherstellen, dass template.getShifts() hier geladen ist,
//                        // sonst gibt es eine LazyInitializationException.
//                        // Das WeeklyTemplate muss EAGER geladen werden oder mit fetch-Join.
//
//                        // Find all template shifts defined for this specific day of the week in the active template
//                        template.getShifts().stream()
//                                .filter(templateShift -> templateShift.getWeekday() == weekdayIndex)
//                                .forEach(ts -> {
//                                    // --- DEBUGGING START ---
//                                    org.roster.backend.domain.Shift originalBaseShift = ts.getShift();
//                                    UUID originalBaseShiftId = (originalBaseShift != null) ? originalBaseShift.getId() : null;
//                                    System.out.println("DEBUG: Generating solver shift. BaseShift ID from TemplateShift: " + originalBaseShiftId);
//                                    if (originalBaseShiftId == null) {
//                                        System.err.println("ERROR: BaseShift ID is NULL here! TemplateShift ID: " + ts.getId());
//                                    }
//                                    // --- DEBUGGING END ---
//
//                                    // Create a new concrete Shift instance for the solver
//                                    Shift solverShift = new Shift(
//                                            UUID.randomUUID(), // Generate a unique ID for this specific shift instance
//                                            ts.getShift().getName() + " (" + ts.getPositionName() + ")", // Combine base shift name and position
//                                            currentDate,
//                                            ts.getShift().getStartTime(),
//                                            ts.getShift().getEndTime(),
//                                            ts.getShift().getId()
//                                    );
//                                    // --- DEBUGGING START ---
//                                    System.out.println("DEBUG: Solver shift created. BaseShift ID in solverShift: " + solverShift.getBaseShiftId());
//                                    // --- DEBUGGING END ---
//                                    concreteShifts.add(solverShift);
//                                });
//                    });
//        }
//
//        // Optional: Sort shifts chronologically, although Timefold doesn't strictly require it
//        concreteShifts.sort(Comparator.comparing(Shift::getStartDateTime));
//
//        System.out.println("Generated " + concreteShifts.size() + " concrete shifts for schema " + schema.getId()); // Logging
//        return concreteShifts;
//    }
//
//
//
//    /**
//     * Creates a new {@code ScheduleProposal} from the given {@code RosterSolution}.
//     * This method processes the solution generated by the solver and converts it
//     * into a persistent scheduling proposal, including mapping shift assignments and
//     * setting the status based on the solver's score.
//     *
//     * @param schema the {@code ScheduleSchema} associated with the scheduling proposal
//     *               to provide schema-related metadata.
//     * @param solution the {@code RosterSolution} generated by the solver, containing
//     *                 shift assignments and scoring information.
//     * @return the saved {@code ScheduleProposal} that includes the mapped shift assignments
//     *         and result status derived from the solver solution.
//     */
//    @Transactional // Teil der Transaktion von 'solve'.
//    protected ScheduleProposal createScheduleProposalFromSolution(ScheduleSchema schema, RosterSolution solution) {
//        ScheduleProposal scheduleProposal = new ScheduleProposal();
//        scheduleProposal.setSchema(schema);
//        scheduleProposal.setGeneratedAt(java.time.LocalDateTime.now());
//        // Status basierend auf dem Score des Solvers setzen (z.B. COMPLETED wenn Hard-Score 0 ist)
//        scheduleProposal.setStatus(solution.getScore().getHardScore() == 0 ? ProposalStatus.COMPLETED : ProposalStatus.FAILED);
//
//        ScheduleProposal savedProposal = proposalPort.save(scheduleProposal);
//
//        List<ScheduleProposalShift> proposalShifts = solution.getShiftAssignments().stream()
//                .map(solverAssignment -> {
//                    // Prüfen, ob eine Zuweisung stattgefunden hat
//                    if (solverAssignment.getEmployee() == null) {
//                        // Ungewiesene Schichten überspringen oder als unzugewiesen speichern
//                        System.out.println("DEBUG: Solver did not assign an employee for shift: " + solverAssignment.getShift().getName());
//                        return null; // Wird später gefiltert
//                    }
//
//                    // Original Backend-Shift finden mit der baseShiftId aus dem Solver-Shift
//                    UUID originalBaseShiftId = solverAssignment.getShift().getBaseShiftId();
//                    if (originalBaseShiftId == null) {
//                        System.err.println("ERROR: Solver Shift has no BaseShiftId for assignment " + solverAssignment.getId());
//                        throw new IllegalStateException("Solver Shift missing original BaseShiftId for mapping!");
//                    }
//                    org.roster.backend.domain.Shift baseShift = shiftPort.findShiftById(originalBaseShiftId)
//                            .orElseThrow(() -> new IllegalStateException("Original backend Shift not found for ID: " + originalBaseShiftId));
//
//                    ScheduleProposalShift proposalShift = new ScheduleProposalShift();
//                    proposalShift.setProposal(savedProposal);
//                    proposalShift.setBaseShift(baseShift);
//                    proposalShift.setDate(solverAssignment.getShift().getStartDateTime().toLocalDate()); // Datum aus LocalDateTime
//                    proposalShift.setPositionName(extractPositionName(solverAssignment.getShift().getName())); // Position extrahieren
//                    proposalShift.setAssignedStaffName(solverAssignment.getEmployee().getName()); // Name des zugewiesenen Mitarbeiters
//                    return proposalShift;
//                })
//                .filter(Objects::nonNull) // Ungewiesene Schichten filtern, falls sie null zurückgeben
//                .collect(Collectors.toList());
//
//        savedProposal.setProposalShifts(proposalShifts);
//        return proposalPort.save(savedProposal); // Speichern mit kaskadierten Shifts
//    }
//}