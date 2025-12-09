package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.AvailabilityEntryDetailDto;
import org.roster.backend.application.service.AvailabilityService; // Unser erweiterter Service
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api") // Verwende /api als Basis, da die Pfade '/schemas/{...}' und '/availability-entries/{...}' spezifischer sind
@RequiredArgsConstructor
public class AvailabilityEntryController {

    private final AvailabilityService availabilityService;

    // --- ENDPUNKT: Verfügbarkeitseinträge für ein Schema abrufen (Wireframe 2 Tabelle) ---
    // GET /api/schemas/{schemaId}/availability-entries
    @GetMapping("/schemas/{schemaId}/availability-entries")
    public ResponseEntity<List<AvailabilityEntryDetailDto>> getAvailabilityEntriesForSchema(@PathVariable UUID schemaId) {
        List<AvailabilityEntryDetailDto> dtos = availabilityService.getAvailabilityEntriesDetailsForSchema(schemaId);
        return ResponseEntity.ok(dtos);
    }

    // --- ENDPUNKT: TargetShiftCount eines Verfügbarkeitseintrags aktualisieren (Wireframe 2 "set target" Button) ---
    // PUT /api/availability-entries/{entryId}/target-shift-count
    @PutMapping("/availability-entries/{entryId}/target-shift-count")
    public ResponseEntity<Void> updateTargetShiftCount(@PathVariable UUID entryId, @RequestBody Integer targetShiftCount) {
        try {
            availabilityService.updateAvailabilityEntryTargetShiftCount(entryId, targetShiftCount);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null); // Null-Body bei 500er Fehler
        }
    }

    // ACHTUNG: Die Endpunkte für 'submitAvailability' und 'getPublicSchema'
    // sind in deinem bestehenden AvailabilityService, aber noch nicht in diesem neuen Controller.
    // Du müsstest entscheiden, ob diese auch hierher verschoben werden sollen
    // (was bei Public-Endpunkten oft Sinn macht) oder im AuthController bleiben.
    // Für dieses Feature sind sie nicht direkt relevant.
}
