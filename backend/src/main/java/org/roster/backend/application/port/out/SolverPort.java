package org.roster.backend.application.port.out;

import org.roster.backend.domain.ScheduleProposal;
import org.roster.backend.domain.ScheduleSchema;

/**
 * Output port for the scheduling engine.
 * Decouples the domain/service layer from the specific solver implementation (Timefold).
 */
public interface SolverPort {
    ScheduleProposal solve(ScheduleSchema schema);
}