package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.ShiftDto;
import org.roster.backend.application.service.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<ShiftDto> createShift(@RequestBody ShiftDto shiftDto) {
        ShiftDto createdShift = shiftService.createShift(shiftDto);
        return new ResponseEntity<>(createdShift, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ShiftDto>> getMyShifts() {
        List<ShiftDto> shifts = shiftService.getShiftsForCurrentUser();
        return ResponseEntity.ok(shifts);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ShiftDto> updateShift(@PathVariable UUID id, @RequestBody ShiftDto shiftDto) {
        try {
            ShiftDto updatedShift = shiftService.updateShift(id, shiftDto);
            return ResponseEntity.ok(updatedShift);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } //catch (SecurityException e) {
           // return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        //}
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteShift(@PathVariable UUID id) {
        try {
            shiftService.deleteShift(id);
            return ResponseEntity.noContent().build(); // 204 No Content ist Standard für erfolgreiches Löschen
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } //catch (IllegalStateException e) {
           // return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict wenn Löschen nicht erlaubt ist
        //}
    }

}