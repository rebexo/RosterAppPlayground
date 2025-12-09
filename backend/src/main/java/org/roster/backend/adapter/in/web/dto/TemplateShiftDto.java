package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class TemplateShiftDto {
    private Integer weekday; // 0=Montag, 6=Sonntag
    private String positionName;
    private UUID shiftId; // Die ID der Basis-Schicht, die hier verwendet wird
}