package org.roster.backend.application.port.out;

import org.roster.backend.domain.AvailabilityEntry;
import org.roster.backend.domain.ScheduleProposal;
import java.util.List;

public interface ExcelExportPort {
    byte[] generateRosterFile(ScheduleProposal proposal, List<AvailabilityEntry> availabilities);
}
