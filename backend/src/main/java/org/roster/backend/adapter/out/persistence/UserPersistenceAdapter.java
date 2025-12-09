package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.UserPort;
import org.roster.backend.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        // Hier nutzen wir die Optional-Rückgabe des Repositories,
        // da keine dedizierte exists-Methode definiert war.
        // Alternativ könntest du im UserRepository `boolean existsByUsername(String username);` ergänzen.
        return userRepository.findByUsername(username).isPresent();
    }
}