package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp; // automatischer timestamp
import org.roster.backend.domain.enums.ProposalStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Repräsentiert einen generierten Dienstplanvorschlag des Timefold-Solvers
 * für ein spezifisches {@link ScheduleSchema}.
 * Ein Proposal enthält eine Sammlung von {@link ScheduleProposalShift}s,
 * die die vom Solver zugewiesenen Schichten darstellen.
 *
 * @author Rebecca Kassaye
 */
@Data
@Entity
@Table(name = "schedule_proposals")
public class ScheduleProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Das {@link ScheduleSchema}, zu dem dieser Vorschlag gehört.
     */
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "schema_id", nullable = false)
    private ScheduleSchema schema;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Lob // Use Lob for potentially long strings like JSON parameters
    private String generationParams; // Optional: Store solver settings or info

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    /**
     * Eine Liste der vom Solver zugewiesenen {@link ScheduleProposalShift}s,
     * die diesen Dienstplanvorschlag bilden.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduleProposalShift> proposalShifts = new ArrayList<>();

}