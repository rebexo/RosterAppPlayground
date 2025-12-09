package org.roster.backend.solver.constraints;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.Joiners;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.roster.backend.solver.domain.Availability;
import org.roster.backend.solver.domain.Employee;
import org.roster.backend.solver.domain.ShiftAssignment;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.toSet;

public class RosterConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard ---------------
                // wenn nicht da ist schlecht arbeiten
                employeeUnavailable(constraintFactory),
                // keine zweiteilung
                conflictingShifts(constraintFactory),
                // nicht übertreuben
                maxTargetShiftCountExceeded(constraintFactory),

                // Soft ---------------
                // Schichten besetzen
                penalizeUnassignedShifts(constraintFactory),
                // verteilen
                minTargetShiftCountNotMet(constraintFactory),
                // Blöcke vermeiden
                penalizeLongConsecutiveStretches(constraintFactory),

        };
    }

    // Harte Regel: Ein Mitarbeiter darf keiner Schicht zugewiesen werden, wenn er an diesem Tag nicht verfügbar ist.
    private Constraint employeeUnavailable(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)
                .filter(assignment -> assignment.getEmployee() != null) // Nur zugewiesene Schichten prüfen
                .join(Availability.class,
                        Joiners.equal(assignment -> assignment.getEmployee().getId(), Availability::getEmployeeId),
                        Joiners.equal(assignment -> assignment.getShift().getStartDateTime().toLocalDate(), Availability::getDate)
                )
                .filter((assignment, availability) -> availability.getStatus() == AvailabilityStatus.UNAVAILABLE)
                .penalize(HardSoftScore.ofHard(10)) // Strafe: 1 harter Punkt
                .asConstraint("Mitarbeiter nicht verfügbar");
    }

    // Harte Regel: Ein Mitarbeiter darf nicht zwei überlappenden Schichten zugewiesen werden.
    private Constraint conflictingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee), // Finde Paare für denselben Mitarbeiter
                        Joiners.overlapping( // Finde Paare, deren Zeiten sich überlappen
                                assignment -> assignment.getShift().getStartDateTime(),
                                assignment -> assignment.getShift().getEndDateTime()
                        )
                )
                .filter((assignment1, assignment2) -> assignment1.getEmployee() != null) // Nur, wenn zugewiesen
                .penalize(HardSoftScore.ofHard(10))
                .asConstraint("Überlappende Schichten");
    }

    // Harte Regel: Ein Mitarbeiter darf seine Ziel-Schichtanzahl nicht überschreiten.
    private Constraint maxTargetShiftCountExceeded(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)

                // 1. Nur zugewiesene Schichten betrachten
                .filter(assignment -> assignment.getEmployee() != null)

                // 2. Gruppiere nach Mitarbeiter und zähle die Schichten
                .groupBy(ShiftAssignment::getEmployee,
                        ConstraintCollectors.count()
                )

                // 3. Ergebnis ist BiStream<Employee, Integer (actualCount)>

                // 4. Bestrafe die Überschreitung als harten Verstoß
                .penalize(HardSoftScore.ONE_HARD, // Harte Strafe
                        (employee, actualCount) -> {

                            int target = employee.getTargetShiftCount();

                            // Prüfe, ob die Ist-Anzahl größer als das Ziel ist
                            if (actualCount > target) {
                                // Ja, Verstoß. Bestrafe mit der Höhe der Überschreitung.
                                // z.B. 2 Schichten zu viel = 2 harte Strafpunkte
                                return actualCount - target;
                            } else {
                                // Kein Verstoß (actualCount <= target)
                                return 0;
                            }
                        })
                .asConstraint("Maximale Ziel-Schichtanzahl überschritten");
    }

    // Weiche Regel: Mitarbeiter sollen ihre Ziel-Schichtanzahl möglichst erreichen.
    private Constraint minTargetShiftCountNotMet(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)

                // 1. Nur zugewiesene Schichten betrachten
                .filter(assignment -> assignment.getEmployee() != null)

                // 2. Gruppiere nach Mitarbeiter und zähle die Schichten
                .groupBy(ShiftAssignment::getEmployee,
                        ConstraintCollectors.count()
                )

                // 3. Ergebnis ist BiStream<Employee, Integer (actualCount)>

                // 4. Bestrafe die UNTERSCHREITUNG als weichen Verstoß
                .penalize(HardSoftScore.ONE_SOFT, // Weiche Strafe
                        (employee, actualCount) -> {

                            int target = employee.getTargetShiftCount();

                            // Prüfe, ob die Ist-Anzahl *kleiner* als das Ziel ist
                            if (actualCount < target) {
                                int missing = target - actualCount;
                                // Ja, Verstoß. Bestrafe mit der Höhe der Unterschreitung.
                                // z.B. 2 Schichten zu wenig = 2 weiche Strafpunkte
                                return missing * missing;
                            } else {
                                // Kein Verstoß (actualCount >= target)
                                return 0;
                            }
                        })
                .asConstraint("Minimale Ziel-Schichtanzahl unterschritten");
    }

    // Weiche Regel: Bestrafe Arbeitsblöcke, die länger als X Tage sind.
    private Constraint penalizeLongConsecutiveStretches(ConstraintFactory factory) {

        // Definiere, wie viele Tage am Stück maximal erlaubt sind,
        // bevor eine Strafe anfällt. 5 oder 6 ist ein typischer Wert.
        final int MAX_ALLOWED_CONSECUTIVE = 2;

        return factory.forEach(ShiftAssignment.class)

                // 1. Nur zugewiesene Schichten betrachten
                .filter(assignment -> assignment.getEmployee() != null)

                // 2. Gruppiere nach Mitarbeiter.
                //    Sammle alle *einzigartigen* Daten (LocalDate), an denen sie arbeiten.
                //    toSet() ist wichtig, falls ein Mitarbeiter 2 Schichten am selben Tag hat
                //    (zählt trotzdem nur als 1 Arbeitstag in der Kette).
                .groupBy(ShiftAssignment::getEmployee,
                        ConstraintCollectors.toSet(assignment -> assignment.getShift().getStartDateTime().toLocalDate())
                )

                // 3. Ergebnis ist BiStream<Employee, Set<LocalDate>>
                //    Bestrafe nun basierend auf diesem Set
                .penalize(HardSoftScore.ONE_SOFT, // Weiche Strafe
                        (employee, workDates) -> {

                            if (workDates.size() <= MAX_ALLOWED_CONSECUTIVE) {
                                // Wenn die Gesamtanzahl der Tage kleiner als das Limit ist,
                                // kann es keinen Verstoß geben. Schneller Ausstieg.
                                return 0;
                            }

                            // Wandle das Set in eine sortierte Liste um
                            List<LocalDate> sortedDates = new ArrayList<>(workDates);
                            Collections.sort(sortedDates);

                            int penalty = 0;
                            int consecutiveDaysInStretch = 1; // Zähler für den aktuellen Block

                            // 4. Iteriere durch die sortierten Tage und zähle Blöcke
                            for (int i = 0; i < sortedDates.size() - 1; i++) {
                                LocalDate currentDay = sortedDates.get(i);
                                LocalDate nextDay = sortedDates.get(i + 1);

                                if (currentDay.plusDays(1).equals(nextDay)) {
                                    // Die Tage sind aufeinanderfolgend
                                    consecutiveDaysInStretch++;
                                } else {
                                    // Der Block ist hier unterbrochen
                                    penalty += calculatePenalty(consecutiveDaysInStretch, MAX_ALLOWED_CONSECUTIVE);
                                    // Setze den Zähler für den nächsten Block zurück
                                    consecutiveDaysInStretch = 1;
                                }
                            }

                            // 5. Prüfe den allerletzten Block am Ende der Schleife
                            penalty += calculatePenalty(consecutiveDaysInStretch, MAX_ALLOWED_CONSECUTIVE);

                            return penalty;
                        })
                .asConstraint("Lange Arbeitsblöcke vermeiden");
    }

    /**
     * Hilfsmethode: Berechnet die Strafe für einen einzelnen Block.
     * Wir verwenden eine quadratische Strafe, um lange Blöcke
     * exponentiell stärker zu bestrafen.
     */
    private int calculatePenalty(int consecutiveDays, int maxAllowed) {
        if (consecutiveDays > maxAllowed) {
            int overage = consecutiveDays - maxAllowed;
            // Quadratische Strafe:
            // 1 Tag drüber (6) -> 1*1 = 1 Strafe
            // 2 Tage drüber (7) -> 2*2 = 4 Strafe
            // ...
            // 7 Tage drüber (12) -> 7*7 = 49 Strafe
            return overage * overage;
        }
        return 0;
    }

    // Weiche Regel: Bestrafe jede Schicht, die keinen Mitarbeiter hat.
    private Constraint penalizeUnassignedShifts(ConstraintFactory factory) {
        return factory.forEach(ShiftAssignment.class)

                // 1. Finde alle Zuweisungen, bei denen der Mitarbeiter 'null' ist
                .filter(assignment -> assignment.getEmployee() == null)

                // 2. Bestrafe dies mit einer weichen Strafe.
                // Der Wert (z.B. 100) sollte hoch genug sein,
                // dass der Solver das Besetzen bevorzugt.
                .penalize(HardSoftScore.ofSoft(100))
                .asConstraint("Unbelegte Schicht");
    }

}