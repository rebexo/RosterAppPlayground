package org.roster.backend.adapter.in.web.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CalculatedShiftDto {
    private String title; // z.B. "Frühschicht (Kasse 1)"
    //private LocalDate date;
    //private LocalTime startTime;
    //private LocalTime endTime;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private UUID baseShiftId;
}