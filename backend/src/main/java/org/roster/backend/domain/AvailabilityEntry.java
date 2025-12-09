package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Repräsentiert eine vollständige Verfügbarkeitseingabe eines Mitarbeiters
 * für ein spezifisches {@link ScheduleSchema}.
 * Diese Entität fasst die Metadaten der Eingabe zusammen (wer hat wann was eingereicht)
 * und verknüpft sie mit den detaillierten Verfügbarkeitsangaben pro Tag
 * (siehe {@link AvailabilityDetail}).
 *
 */
@Data
@Entity
@Table(name = "availability_entries")
public class AvailabilityEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String staffName;

    private String comment;

    /**
     * Die Zielanzahl von Schichten, die er/sie diesem Dienstplanschema übernehmen sollte.
     * Wird später vom {@link User} Planer festgelegt.
     */
    private Integer targetShiftCount;

    private LocalDateTime submittedAt;

    /**
     * Das {@link ScheduleSchema}, für das diese Verfügbarkeitseingabe gilt.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "schema_id", nullable = false)
    private ScheduleSchema schema;


    /**
     * Eine Liste der detaillierten Verfügbarkeitsangaben pro Tag.
     * Dies ist eine One-to-Many-Beziehung, da eine Verfügbarkeitseingabe viele Details (für verschiedene Tage)
     * enthalten kann.
     */
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilityDetail> details = new ArrayList<>();
}