package org.roster.backend.adapter.out.persistence;

import org.roster.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Findet einen Benutzer anhand seines einzigartigen Benutzernamens.
     * Spring Data JPA erstellt die Abfrage automatisch aus dem Methodennamen.
     *
     * @param username Der zu suchende Benutzername.
     * @return Ein Optional, das den gefundenen Benutzer enthält oder leer ist.
     */
    Optional<User> findByUsername(String username);
}