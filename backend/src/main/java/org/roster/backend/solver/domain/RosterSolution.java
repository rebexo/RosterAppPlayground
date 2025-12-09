package org.roster.backend.solver.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@PlanningSolution // Sagt Timefold: Das ist das Planungsproblem/-ergebnis
@Data
@NoArgsConstructor
public class RosterSolution {

    // --- Problem Facts ---
    @ProblemFactCollectionProperty // Feste Fakten: Liste aller Mitarbeiter
    @ValueRangeProvider(id = "employeeRange") // Sagt Timefold: Hier findest du die möglichen Werte für die Planning Variable
    private List<Employee> employees;

    @ProblemFactCollectionProperty // Feste Fakten: Liste aller konkreten Schichten
    private List<Shift> shifts;

    @ProblemFactCollectionProperty // Feste Fakten: Liste aller Verfügbarkeiten
    private List<Availability> availabilities;

    // --- Planning Entities ---
    @PlanningEntityCollectionProperty // Das, was geplant wird: Die Schichtzuweisungen
    private List<ShiftAssignment> shiftAssignments;

    // --- Score ---
    @PlanningScore // Hier speichert Timefold das Ergebnis
    private HardSoftScore score;

    public RosterSolution(List<Employee> employees, List<Shift> shifts, List<Availability> availabilities, List<ShiftAssignment> shiftAssignments) {
        this.employees = employees;
        this.shifts = shifts;
        this.availabilities = availabilities;
        this.shiftAssignments = shiftAssignments;
    }
}