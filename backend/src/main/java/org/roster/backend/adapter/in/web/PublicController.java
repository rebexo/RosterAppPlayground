package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.NewAvailabilityEntryDto;
import org.roster.backend.adapter.in.web.dto.PublicSchemaDto;
import org.roster.backend.application.port.in.iAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/schemas")
@RequiredArgsConstructor
public class PublicController {

    private final iAvailabilityService availabilityService;

    @GetMapping("/{linkId}")
    public ResponseEntity<?> getPublicSchemaData(@PathVariable String linkId) {
        try {
            PublicSchemaDto schemaDto = availabilityService.getPublicSchema(linkId);
            return ResponseEntity.ok(schemaDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/{linkId}")
    public ResponseEntity<?> submitAvailability(@PathVariable String linkId, @RequestBody NewAvailabilityEntryDto entryDto) {
        try {
            availabilityService.submitAvailability(linkId, entryDto);
            return ResponseEntity.ok("Vielen Dank! Deine Verfügbarkeiten wurden erfolgreich übermittelt.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}