package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
import org.roster.backend.domain.enums.AvailabilityStatus;
import java.time.LocalDate;

@Data
public class AvailabilityDetailDto {
    private LocalDate date;
    private AvailabilityStatus status;
}