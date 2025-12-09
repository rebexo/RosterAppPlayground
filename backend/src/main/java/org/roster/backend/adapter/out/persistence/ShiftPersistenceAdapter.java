package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.ShiftPort;
import org.roster.backend.domain.Shift;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementation for managing {@link Shift} entities.
 * <p>
 * This class serves as the bridge between the domain layer (defined by {@link ShiftPort})
 * and the infrastructure layer. It routes domain operations to the appropriate
 * JPA repositories, specifically {@link ShiftRepository} and {@link TemplateShiftRepository}.
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Executing CRUD operations for shifts.</li>
 * <li>Validating shift usage within templates to prevent data inconsistency.</li>
 * </ul>
 * <p>
 * Note: This class is a Spring-managed {@code @Component} using constructor injection.
 *
 * @see ShiftPort
 * @see ShiftRepository
 */
@Component
@RequiredArgsConstructor
public class ShiftPersistenceAdapter implements ShiftPort {

    private final ShiftRepository shiftRepository;
    private final TemplateShiftRepository templateShiftRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Shift saveShift(Shift shift) { return shiftRepository.save(shift); }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Shift> findAllShifts() {
        return shiftRepository.findAll();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Shift> findShiftById(UUID id) {
        return shiftRepository.findById(id);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteShift(Shift shift) {
        shiftRepository.delete(shift);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation Note:</b><br>
     * Delegates to {@link TemplateShiftRepository#existsByShiftId(UUID)}.
     */
    @Override
    public boolean isShiftUsedInTemplate(UUID shiftId) {
        return templateShiftRepository.existsByShiftId(shiftId);
    }
}