package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Repräsentiert eine generische Schichtdefinition (Basisschicht oder Vorlage).
 * Eine Schicht definiert einen Namen, eine Start- und Endzeit und ist einem {@link User} (Planer) zugeordnet.
 * Diese Definitionen werden später in konkrete {@link org.roster.backend.solver.domain.Shift} Instanzen
 * für den Solver übersetzt, die dann einem Datum zugeordnet sind.
 *
 */
@Data
@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Der Anzeigename der Schicht, z.B. "Frühschicht", "Spätschicht", "Nachtdienst".
     */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * Der {@link User}, der diese Schichtdefinition erstellt und besitzt.
     * Ein Planer kann mehrere Schichtdefinitionen haben.
     */
    @ManyToOne(optional = false) // Eine Schicht muss immer einem Planer zugeordnet sein
    @JoinColumn(name = "planner_id", nullable = false) // Fremdschlüsselspalte in der Datenbank
    private User planner;

    /**
     * Prüft, ob diese Schicht tagesübergreifend ist.
     * @return {@code true}, wenn die Schicht tagesübergreifend ist, sonst {@code false}.
     */
    @Transient // nicht in der DB gespeichert
    public boolean isOvernightShift() {
        return endTime.isBefore(startTime);
    }
}