package org.roster.backend.application.port.in;

import org.roster.backend.adapter.in.web.dto.ShiftDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface iShiftService {
    ShiftDto createShift(ShiftDto shiftDto);

    @Transactional(readOnly = true)
    List<ShiftDto> getShiftsForCurrentUser();

    ShiftDto updateShift(UUID shiftId, ShiftDto shiftDto);

    void deleteShift(UUID shiftId);
}
