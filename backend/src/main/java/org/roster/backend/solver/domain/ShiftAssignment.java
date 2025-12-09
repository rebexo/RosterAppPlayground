package org.roster.backend.solver.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@PlanningEntity //to be planned
@Data
@NoArgsConstructor
public class ShiftAssignment {

    @PlanningId
    private UUID id; // Eindeutige ID der Zuweisung

    private Shift shift; // Die konkrete Schicht, die besetzt werden soll

    // Die Planning Variable: Timefold versucht, hier einen Employee zuzuweisen
    @PlanningVariable(valueRangeProviderRefs = {"employeeRange"})// allowsUnassigned = true
    private Employee employee; // Kann anfangs null sein

    // Getter für EmployeeId und ShiftId, um Joiner zu vereinfachen
    public UUID getEmployeeId() {
        return (employee != null) ? employee.getId() : null;
    }

    public UUID getShiftId() {
        return (shift != null) ? shift.getId() : null;
    }

    public ShiftAssignment(UUID id, Shift shift, Employee employee) {
        this.id = id;
        this.shift = shift;
        this.employee = employee;
    }

    public ShiftAssignment(UUID id, Shift shift) {
        this.id = id;
        this.shift = shift;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShiftAssignment that = (ShiftAssignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return shift.getName() + " am " + shift.getStartDateTime().toLocalDate() + " -> " + (employee != null ? employee.getName() : "unassigned");
    }

}