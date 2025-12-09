package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findByPlannerId(Long id);
}