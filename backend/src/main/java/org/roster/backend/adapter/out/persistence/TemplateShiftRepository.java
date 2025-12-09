package org.roster.backend.adapter.out.persistence;
import org.roster.backend.domain.TemplateShift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TemplateShiftRepository extends  JpaRepository<TemplateShift, Long> {
    /**
     * Checks if any TemplateShift entity exists that uses the given shiftId.
     * Spring Data JPA creates the query automatically from the method name.
     * @param shiftId The ID of the Shift to check for.
     * @return true if the shift is in use, false otherwise.
     */
    boolean existsByShiftId(UUID shiftId);
}
