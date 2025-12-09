package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "schedule_proposal_shifts")
public class ScheduleProposalShift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // Renamed from proposalShiftID

    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ScheduleProposal proposal; // Link back to the parent proposal

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "base_shift_id", nullable = false) // Use a different name to avoid conflict if Shift is also mapped directly
    private Shift baseShift; // Link to the original Shift definition

    @Column(nullable = false)
    private LocalDate date; // The specific date this shift occurs on

    private String positionName; // Store the position name directly for easier lookup

    private String assignedStaffName; // Name of the employee assigned by the solver (can be null if unassigned)

}