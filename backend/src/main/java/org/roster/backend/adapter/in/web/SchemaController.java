package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.SchemaDto;
import org.roster.backend.adapter.in.web.dto.CalculatedShiftDto;
import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.application.service.SchemaService;
import org.roster.backend.application.service.SolverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schemas")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;
    private final SolverService solverService;

    @PostMapping
    public ResponseEntity<SchemaDto> createSchema(@RequestBody SchemaDto schemaDto) {
        SchemaDto createdSchema = schemaService.createSchema(schemaDto);
        return new ResponseEntity<>(createdSchema, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SchemaDto>> getMySchemas() {
        List<SchemaDto> schemas = schemaService.getSchemasForCurrentUser();
        return ResponseEntity.ok(schemas);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<CalculatedShiftDto>> getSchemaDetails(@PathVariable UUID id) {
        try {
            List<CalculatedShiftDto> details = schemaService.getSchemaDetails(id);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchemaDto> getSchemaById(@PathVariable UUID id) {
        try {
            SchemaDto schema = schemaService.getSchemaById(id);
            return ResponseEntity.ok(schema);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Allgemeine Fehlerbehandlung
            return ResponseEntity.internalServerError().body(null);
        }
    }



    @PatchMapping("/{id}/expected-entries")
    public ResponseEntity<SchemaDto> updateExpectedEntries(@PathVariable UUID id, @RequestBody Integer count) {
        try {
            SchemaDto updatedSchema = schemaService.updateExpectedEntries(id, count);
            return ResponseEntity.ok(updatedSchema);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/generate_proposal")
    public ResponseEntity<?> generateProposal(@PathVariable UUID id) {
        try {
            ScheduleProposal proposal = solverService.solve(id);
            // Gib die Lösung zurück (oder nur eine Bestätigung)
            return ResponseEntity.ok(proposal); // Evtl. nur Score zurückgeben.
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Fehler bei der Planerstellung: " + e.getMessage());
        }
    }

}