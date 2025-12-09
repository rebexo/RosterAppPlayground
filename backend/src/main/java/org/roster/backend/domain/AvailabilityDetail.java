package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.roster.backend.domain.enums.AvailabilityStatus;
import java.time.LocalDate;

/**
 * Repräsentiert die detaillierte Verfügbarkeitsangabe eines Mitarbeiters
 * für einen spezifischen Tag innerhalb einer {@link AvailabilityEntry}.
 * Jedes Detail speichert das Datum und den vom Mitarbeiter gewählten
 * {@link AvailabilityStatus} für diesen Tag.
 *
 */
@Data
@Entity
@Table(name = "availability_details")
public class AvailabilityDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    /**
     * Der Verfügbarkeitsstatus des Mitarbeiters für das angegebene {@link #date}.
     * Definiert durch das {@link AvailabilityStatus}-Enum
     * (im Moment nur UNAVAILABLE, aber denkbar wären auch PREFERRED etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;

    /**
     * Der übergeordnete {@link AvailabilityEntry}-Eintrag, zu dem dieses Detail gehört.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private AvailabilityEntry entry;
}