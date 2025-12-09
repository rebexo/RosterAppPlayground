package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Repräsentiert eine spezifische Schicht innerhalb einer {@link WeeklyTemplate}.
 * Eine TemplateShift definiert, an welchem Wochentag, für welche Position
 * und basierend auf welcher {@link Shift}-Definition eine Schicht in der Vorlage
 * eingeplant werden soll.
 *
 */
@Data
@Entity
@Table(name = "template_shifts")
public class TemplateShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der Wochentag, an dem diese Schicht in der {@link WeeklyTemplate} eingeplant ist.
     */
    @Column(nullable = false)
    private Integer weekday; // 0=Montag, 1=Dienstag, etc.

    /**
     * Der Name der Position, die diese Schicht abdeckt.
     */
    @Column(nullable = false)
    private String positionName; // z.B. "Kasse 1", "Lager"

    /**
     * Die {@link WeeklyTemplate}, zu der diese TemplateShift-Definition gehört.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private WeeklyTemplate template;

    /**
     * Die Basis-{@link Shift}-Definition, auf der diese TemplateShift basiert.
     * Viele TemplateShifts können dieselbe Basis-Schicht (z.B. "Frühschicht") referenzieren können.
     * Eine TemplateShift muss auf einer bestehenden Shift-Definition basieren.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
}