package org.roster.backend.adapter.in.web.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SchemaDto {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String availabilityLinkID; // Wird vom Server generiert
    private boolean collectionClosed;
    private Integer expectedEntries;
    private int submittedEntriesCount;
    private List<SchemaTemplateAssignmentDto> templateAssignments;
}