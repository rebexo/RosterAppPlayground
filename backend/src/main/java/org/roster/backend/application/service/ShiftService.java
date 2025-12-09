package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iShiftService;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.adapter.in.web.dto.ShiftDto;
import org.roster.backend.domain.Shift;
import org.roster.backend.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional // Sorgt dafür, dass alle DB-Operationen in einer Transaktion laufen
public class ShiftService implements iShiftService {

    private final ShiftPort shiftPort;

    @Override
    public ShiftDto createShift(ShiftDto shiftDto) {
        // 1. Finde den aktuell angemeldeten User heraus
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Erstelle eine neue Shift-Entity aus dem DTO
        Shift newShift = new Shift();
        newShift.setName(shiftDto.getName());
        newShift.setStartTime(shiftDto.getStartTime());
        newShift.setEndTime(shiftDto.getEndTime());
        newShift.setPlanner(currentUser); // 3. Ordne die Schicht dem User zu

        // 4. Speichere die neue Schicht in der Datenbank
        Shift savedShift = shiftPort.saveShift(newShift);

        // 5. Wandle die gespeicherte Entity zurück in ein DTO für die Antwort
        return mapToDto(savedShift);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ShiftDto> getShiftsForCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return shiftPort.findAllShifts().stream()
                .filter(shift -> shift.getPlanner().getId().equals(currentUser.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ShiftDto updateShift(UUID shiftId, ShiftDto shiftDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Shift shiftToUpdate = shiftPort.findShiftById(shiftId)
                .orElseThrow(() -> new RuntimeException("Schicht nicht gefunden"));

        // Sicherheitsprüfung: Gehört diese Schicht dem eingeloggten User?
        if (!shiftToUpdate.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Sie dürfen diese Schicht nicht bearbeiten.");
        }

        shiftToUpdate.setName(shiftDto.getName());
        shiftToUpdate.setStartTime(shiftDto.getStartTime());
        shiftToUpdate.setEndTime(shiftDto.getEndTime());

        Shift updatedShift = shiftPort.saveShift(shiftToUpdate);
        return mapToDto(updatedShift);
    }


    @Override
    public void deleteShift(UUID shiftId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Shift shiftToDelete = shiftPort.findShiftById(shiftId)
                .orElseThrow(() -> new RuntimeException("Schicht nicht gefunden"));

        // Sicherheitsprüfung
        if (!shiftToDelete.getPlanner().getId().equals(currentUser.getId())) {
            throw new SecurityException("Sie dürfen diese Schicht nicht löschen.");
        }

        // Prüfung aus US-17: Wird die Schicht in einem Template verwendet?
        if (shiftPort.isShiftUsedInTemplate(shiftId)) {
            throw new IllegalStateException("Diese Schicht kann nicht gelöscht werden, da sie in einem Template verwendet wird.");
        }

        shiftPort.deleteShift(shiftToDelete);
    }

    // Private Hilfsmethode zur Konvertierung
    private ShiftDto mapToDto(Shift shift) {
        ShiftDto dto = new ShiftDto();
        dto.setId(shift.getId());
        dto.setName(shift.getName());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        return dto;
    }
}