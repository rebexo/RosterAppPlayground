package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssignedShiftDto {
    private LocalDate date;
    private String shiftName;
    private String positionName;
    private String staffName;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}