package org.roster.backend.solver.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;


/*
* Class for (somewhat simplified) representation of the AvailabilityEntry
* */
@Data
@NoArgsConstructor // Wichtig für Timefold
public class Employee {
    @PlanningId // Eindeutige ID für Timefold
    private UUID id;
    private String name;
    private Integer targetShiftCount; // Zielanzahl aus US-07

    public Employee(UUID id, String name, Integer targetShiftCount) {
        this.id = id;
        this.name = name;
        this.targetShiftCount = targetShiftCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}