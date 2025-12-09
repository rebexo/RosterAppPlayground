package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.AvailabilityDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityDetailRepository extends JpaRepository<AvailabilityDetail, Long> {
    // Bleibt vorerst leer. Spring Data JPA stellt die Standardmethoden bereit.
    // Wir brauchen es hauptsächlich, damit der DataSeeder die Details löschen kann.
}