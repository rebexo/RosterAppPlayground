package org.roster.backend.application.port.out;

import org.roster.backend.domain.Shift;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * Output port interface defining the persistence contract for {@link Shift} entities.
 * <p>
 * This interface acts as a gateway between the domain layer and the persistence mechanism (database).
 * Adapters implementing this interface are responsible for the actual mapping and storage.
 */
public interface ShiftPort {

    /**
     * Persists the given {@link Shift} entity.
     * <p>
     * If the shift represents a new entity, it will be created.
     * If it already exists, the existing record will be updated.
     *
     * @param shift the domain entity to save; must not be {@code null}
     * @return the saved {@link Shift} entity (potentially with generated fields like IDs)
     */
    Shift saveShift(Shift shift);

    /**
     * Retrieves all available {@link Shift} entities from the repository.
     *
     * @return a {@link List} of all shifts; returns an empty list if no shifts are found
     */
    List<Shift> findAllShifts();

    /**
     * Retrieves a specific {@link Shift} by its unique identifier.
     *
     * @param id the unique {@link UUID} of the shift to find
     * @return an {@link Optional} containing the shift if found, or {@code Optional.empty()} if not
     */
    Optional<Shift> findShiftById(UUID id);

    /**
     * Removes the specified {@link Shift} from the persistence store.
     *
     * @param shift the entity to delete
     */
    void deleteShift(Shift shift);

    /**
     * Checks if the specified shift is currently referenced by any roster template.
     * <p>
     * This method is used to enforce referential integrity rules before
     * deleting or modifying a shift.
     *
     * @param shiftId the unique identifier of the shift to check
     * @return {@code true} if the shift is associated with at least one template, {@code false} otherwise
     */
    boolean isShiftUsedInTemplate(UUID shiftId);
}