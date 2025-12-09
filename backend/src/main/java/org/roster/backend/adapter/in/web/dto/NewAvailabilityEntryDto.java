package org.roster.backend.adapter.in.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewAvailabilityEntryDto {
    private String staffName;
    private String comment;
    private Integer targetShiftCount;
    private List<AvailabilityDetailDto> details;
}