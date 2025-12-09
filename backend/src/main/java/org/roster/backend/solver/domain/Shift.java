package org.roster.backend.solver.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Shift {
    @PlanningId
    private UUID id; // Eine neue, eindeutige ID für DIESE Instanz
    private String name; // z.B. "Frühschicht (Kasse 1)" oder "Bar 2"
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private UUID baseShiftId;

//    public Shift(UUID id, String name, LocalDate date, LocalTime startTime, LocalTime endTime, UUID baseShiftId) {
//        this.id = id;
//        this.name = name;
//        this.startDateTime = LocalDateTime.of(date, startTime);
//        this.endDateTime = LocalDateTime.of(date, endTime);
//        this.baseShiftId = baseShiftId;
//    }

    public Shift(UUID id, String name, LocalDate shiftDate, LocalTime startTime, LocalTime endTime, UUID baseShiftId) {
        this.id = id;
        this.name = name;
        this.baseShiftId = baseShiftId;
        this.startDateTime = shiftDate.atTime(startTime);

        if (endTime.isBefore(startTime)) {
            this.endDateTime = shiftDate.plusDays(1).atTime(endTime);
        } else {
            this.endDateTime = shiftDate.atTime(endTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return Objects.equals(id, shift.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}