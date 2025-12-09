package org.roster.backend.domain.enums;

public enum ProposalStatus {
    PENDING,    // Just created, not yet solved
    SOLVING,    // Solver is currently running
    COMPLETED,  // Solver finished successfully
    FAILED      // Solver encountered an error
}