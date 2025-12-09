package org.roster.backend.application.port.in;

import org.roster.backend.domain.ScheduleProposal;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface iSolverService {
    @Transactional
    ScheduleProposal solve(UUID schemaId);
}
