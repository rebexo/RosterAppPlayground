package org.roster.backend.solver.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.roster.backend.domain.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Availability {
    private UUID employeeId;
    private LocalDate date;
    private AvailabilityStatus status; // UNAVAILABLE oder PREFERRED

    public Availability(UUID employeeId, LocalDate date, AvailabilityStatus status) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Availability that = (Availability) o;
        return  Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, date);
    }
}