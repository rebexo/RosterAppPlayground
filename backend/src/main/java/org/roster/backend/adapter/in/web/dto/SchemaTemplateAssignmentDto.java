package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SchemaTemplateAssignmentDto {
    private UUID templateId;
    private LocalDate validFrom;
    private LocalDate validTo;
}