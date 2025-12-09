package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.TemplateDto;
import org.roster.backend.application.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateDto> createTemplate(@RequestBody TemplateDto templateDto) {
        TemplateDto createdTemplate = templateService.createTemplate(templateDto);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TemplateDto>> getMyTemplates() {
        List<TemplateDto> templates = templateService.getTemplatesForCurrentUser();
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> updateTemplate(@PathVariable UUID id, @RequestBody TemplateDto templateDto) {
        try {
            TemplateDto updated = templateService.updateTemplate(id, templateDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            // Vereinfachtes Error Handling
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable UUID id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}