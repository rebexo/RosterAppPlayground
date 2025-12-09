package org.roster.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Repräsentiert einen konkrete Dienstplan-Instanz für einen definierten Zeitraum.
 * Diese Entität bündelt die {@link SchemaTemplateAssignment}s (welche Vorlagen gelten wann)
 * und dient als Sammelpunkt für die {@link AvailabilityEntry}s der Mitarbeiter.
 */
@Data // Lombok Annotation: Generiert Getter, Setter, equals, hashCode und toString
@Entity // Zeigt an, dass diese Klasse eine JPA-Entität ist
@Table(name = "schedule_schemas") // Definiert den Tabellennamen in der Datenbank
public class ScheduleSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Ein einzigartiger String (NanoID o.ä.), der verwendet wird,
     * um einen öffentlichen Link für Mitarbeiter zur Eingabe ihrer Verfügbarkeit zu bilden.
     * Dieser Link ist spezifisch für dieses Dienstplanschema (US-05).
     */
    @Column(unique = true, nullable = false)
    private String availabilityLinkID;

    /**
     * Die vom Planer festgelegte, erwartete Anzahl an Mitarbeiter-Antworten (US-06).
     * Dient als Referenzwert in der UI.
     */
    private Integer expectedEntries;

    /**
     * Zählt automatisch die Anzahl der eingegangenen {@link AvailabilityEntry}s,
     * die mit diesem Schema verknüpft sind.
     * Diese Berechnung erfolgt direkt in der Datenbank über eine SQL-Formel.
     */
    @Formula("(select count(*) from availability_entries ae where ae.schema_id = id)")
    private int submittedEntriesCount;

    /**
     * Ein Flag, das angibt, ob die Sammlung von Verfügbarkeiten über den
     * {@link #availabilityLinkID} noch aktiv ist (`false`) oder bereits
     * geschlossen wurde (`true`). Wenn `true`, können Mitarbeiter keine weiteren
     * Verfügbarkeiten mehr einreichen oder ändern.
     */
    private boolean collectionClosed = false;

    /**
     * Der {@link User}, der dieses Dienstplanschema erstellt hat und verwaltet.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "planner_id", nullable = false)
    private User planner;

    /**
     * Eine Liste von {@link SchemaTemplateAssignment}s, die definieren, welche
     * Schichtvorlagen an welchen spezifischen Tagen innerhalb dieses Schemas gelten.
     * {@code cascade = CascadeType.ALL} stellt sicher, dass Operationen wie Speichern
     * oder Löschen auf dem Schema auch auf den zugehörigen Zuweisungen ausgeführt werden.
     * {@code orphanRemoval = true} löscht Zuweisungen, die nicht mehr mit einem Schema verknüpft sind.
     * {@code @ToString.Exclude} verhindert eine rekursive Ausgabe in der toString()-Methode von Lombok.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "schema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchemaTemplateAssignment> templateAssignments = new ArrayList<>();
}