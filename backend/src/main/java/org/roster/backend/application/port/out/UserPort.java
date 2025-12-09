package org.roster.backend.application.port.out;

import org.roster.backend.domain.User;

import java.util.Optional;

public interface UserPort {

    /**
     * Findet einen Benutzer anhand seines Benutzernamens.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Speichert einen Benutzer (z.B. bei der Registrierung).
     */
    User saveUser(User user);

    /**
     * Prüft, ob ein Benutzername bereits vergeben ist.
     */
    boolean existsByUsername(String username);

}
