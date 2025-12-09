package org.roster.backend.adapter.in.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProposalDetailDto {
    private String schemaName; // z.B. "Ginst Nov25"
    private List<EmployeeSummaryDto> employeeSummary;
    private List<AssignedShiftDto> assignedShifts;
}