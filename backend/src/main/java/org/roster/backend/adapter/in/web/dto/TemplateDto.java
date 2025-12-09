package org.roster.backend.adapter.in.web.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TemplateDto {
    private UUID id;
    private String name;
    private String description;
    private List<TemplateShiftDto> shifts;
}