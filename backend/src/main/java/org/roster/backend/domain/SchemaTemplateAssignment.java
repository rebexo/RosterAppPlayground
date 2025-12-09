package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Repräsentiert die Zuweisung einer {@link WeeklyTemplate} zu einem {@link ScheduleSchema}
 * für einen spezifischen Gültigkeitszeitraum.
 * Diese Entität definiert, welche Wochenvorlage an welchen Tagen innerhalb eines Dienstplanschemas
 * angewendet werden soll, um die geplanten Schichten für diese Tage zu bestimmen.
 *
 * @author Rebecca Kassaye
 */
@Data
@Entity
@Table(name = "schema_template_assignments")
public class SchemaTemplateAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validTo;

    /**
     * Das {@link ScheduleSchema}, zu dem diese Wochenvorlage zugewiesen wird.
     */
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "schema_id", nullable = false)
    private ScheduleSchema schema;

    /**
     * Die {@link WeeklyTemplate}, die diesem Dienstplanschema für den definierten Zeitraum
     * zugewiesen wird.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private WeeklyTemplate template;
}