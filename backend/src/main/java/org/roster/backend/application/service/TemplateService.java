package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iTemplateService;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.application.port.out.TemplatePort;
import org.roster.backend.adapter.in.web.dto.TemplateDto;
import org.roster.backend.adapter.in.web.dto.TemplateShiftDto;
import org.roster.backend.domain.Shift;
import org.roster.backend.domain.TemplateShift;
import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService implements iTemplateService {

    private final TemplatePort templatePort;
    private final ShiftPort shiftPort;

    @Override
    public TemplateDto createTemplate(TemplateDto templateDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Prüfen, ob ein Template mit dem Namen bereits existiert (US-03, Szenario 2)
        if (templatePort.existsByNameAndPlanner(templateDto.getName(), currentUser, templateDto.getId())){
            throw new IllegalStateException("Ein Template mit diesem Namen existiert bereits.");
        }
        // 1. Haupt-Entität erstellen
        WeeklyTemplate newTemplate = new WeeklyTemplate();
        newTemplate.setName(templateDto.getName());
        newTemplate.setDescription(templateDto.getDescription());
        newTemplate.setPlanner(currentUser);

        // 2. Verschachtelte Schicht-Zuweisungen verarbeiten
        List<TemplateShift> templateShifts = templateDto.getShifts().stream()
                .map(dto -> {
                    // Finde die Basis-Schicht in der DB
                    Shift baseShift = shiftPort.findShiftById(dto.getShiftId())
                            .orElseThrow(() -> new IllegalArgumentException("Shift mit ID " + dto.getShiftId() + " nicht gefunden."));

                    // Erstelle die Zuweisungs-Entität
                    TemplateShift templateShift = new TemplateShift();
                    templateShift.setWeekday(dto.getWeekday());
                    templateShift.setPositionName(dto.getPositionName());
                    templateShift.setShift(baseShift);
                    templateShift.setTemplate(newTemplate); // Verknüpfung zum übergeordneten Template
                    return templateShift;
                }).collect(Collectors.toList());

        newTemplate.setShifts(templateShifts);

        // 3. Speichern. Dank CascadeType.ALL werden alle Kind-Elemente mitgespeichert.
        WeeklyTemplate savedTemplate = templatePort.saveTemplate(newTemplate);

        return mapToDto(savedTemplate);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TemplateDto> getTemplatesForCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return templatePort.findAllTemplates().stream()
                .filter(template -> template.getPlanner().getId().equals(currentUser.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public TemplateDto updateTemplate(UUID templateId, TemplateDto templateDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WeeklyTemplate template = templatePort.findTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Template nicht gefunden"));

        if (!template.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Zugriff verweigert.");
        }

        // Update der einfachen Felder
        template.setName(templateDto.getName());
        template.setDescription(templateDto.getDescription());

        // Update der Schicht-Zuweisungen (Delete-and-Recreate-Strategie)
        template.getShifts().clear(); // Alte Zuweisungen entfernen

        templateDto.getShifts().forEach(dto -> {
            Shift baseShift = shiftPort.findShiftById(dto.getShiftId()).orElseThrow(/*...*/);
            TemplateShift ts = new TemplateShift();
            ts.setWeekday(dto.getWeekday());
            ts.setPositionName(dto.getPositionName());
            ts.setShift(baseShift);
            ts.setTemplate(template);
            template.getShifts().add(ts);
        });

        WeeklyTemplate updatedTemplate = templatePort.saveTemplate(template);
        return mapToDto(updatedTemplate);
    }

    // DELETE
    @Override
    public void deleteTemplate(UUID templateId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WeeklyTemplate template = templatePort.findTemplateById(templateId)
                .orElseThrow(() -> new RuntimeException("Template nicht gefunden"));

        if (!template.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Zugriff verweigert.");
        }

        // US-15: Darf nicht gelöscht werden, wenn es verwendet wird
        if (templatePort.isTemplateUsedInSchema(templateId)) {
            throw new IllegalStateException("Template wird in einem Dienstplan verwendet und kann nicht gelöscht werden.");
        }

        templatePort.deleteTemplate(template);
    }

    // Hilfsmethode zur Konvertierung
    private TemplateDto mapToDto(WeeklyTemplate template) {
        TemplateDto dto = new TemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());

        List<TemplateShiftDto> shiftDtos = template.getShifts().stream()
                .map(ts -> {
                    TemplateShiftDto tsDto = new TemplateShiftDto();
                    tsDto.setWeekday(ts.getWeekday());
                    tsDto.setPositionName(ts.getPositionName());
                    tsDto.setShiftId(ts.getShift().getId());
                    return tsDto;
                }).collect(Collectors.toList());
        dto.setShifts(shiftDtos);

        return dto;
    }
}