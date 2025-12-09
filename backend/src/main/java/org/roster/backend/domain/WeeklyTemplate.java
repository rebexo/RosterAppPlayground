package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Repräsentiert eine Wochenvorlage für die Dienstplanung.
 * Eine WeeklyTemplate bündelt eine Sammlung von {@link TemplateShift}s,
 * die festlegen, welche Schichten an welchen Wochentagen für bestimmte Positionen
 * in einer Woche benötigt werden.
 * Sie wird von einem {@link User} (Planer) erstellt und kann später einem
 * {@link ScheduleSchema} zugewiesen werden.
 **/
@Data
@Entity
@Table(name = "weekly_templates")
public class WeeklyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * Der Anzeigename der Wochenvorlage, z.B. "Standard-Woche", "Ferienzeit".
     */
    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "planner_id", nullable = false)
    private User planner;

    /**
     * Eine Liste von {@link TemplateShift}s, die diese Wochenvorlage definieren.
     * Jede TemplateShift beschreibt eine spezifische Schicht an einem Wochentag.
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) //fetch = FetchType.EAGER
    private List<TemplateShift> shifts = new ArrayList<>();
}