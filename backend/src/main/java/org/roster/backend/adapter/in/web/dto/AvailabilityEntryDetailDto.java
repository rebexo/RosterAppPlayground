package org.roster.backend.adapter.in.web.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityEntryDetailDto {
    private UUID id; // ID des AvailabilityEntry
    private String staffName;
    private Integer targetShiftCount;
    private List<LocalDate> unavailableDates;
    // List<LocalDate> preferredDates; // Optional/later
}