package org.roster.backend.application.port.in;

import org.roster.backend.adapter.in.web.dto.TemplateDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface iTemplateService {
    TemplateDto createTemplate(TemplateDto templateDto);

    @Transactional(readOnly = true)
    List<TemplateDto> getTemplatesForCurrentUser();

    // UPDATE
    TemplateDto updateTemplate(UUID templateId, TemplateDto templateDto);

    // DELETE
    void deleteTemplate(UUID templateId);
}
