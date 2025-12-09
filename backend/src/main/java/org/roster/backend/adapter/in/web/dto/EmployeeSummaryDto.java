package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
@Data
public class EmployeeSummaryDto {
    private String staffName;
    private int targetCount; // Anzahl Zielschichten
    private int actualCount; // Anzahl tatsächliche Schichten
}