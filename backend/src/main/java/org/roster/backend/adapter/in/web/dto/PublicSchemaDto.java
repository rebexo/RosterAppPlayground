package org.roster.backend.adapter.in.web.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PublicSchemaDto {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}