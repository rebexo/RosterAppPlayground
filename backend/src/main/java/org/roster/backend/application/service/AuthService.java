package org.roster.backend.application.service;

import org.roster.backend.application.port.in.iAuthService;
import org.roster.backend.application.port.out.UserPort;
import org.roster.backend.domain.User;
import org.roster.backend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor // Lombok erstellt einen Konstruktor für alle final-Felder
public class AuthService implements iAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class); // Logger hinzufügen
    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public User register(String username, String password) {
        if (userPort.findUserByUsername(username).isPresent()) {
            throw new IllegalStateException("Username wird bereits verwendet.");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.PLANNER);
        return userPort.saveUser(newUser);
    }

    @Override
    public String login(String username, String password) {
        log.info("AuthService.login: Versuch für User '{}' mit Passwortlänge '{}'.", username, password.length()); // LOG 4
        // Dieser Schritt validiert den Usernamen und das Passwort.
        // Wenn die Daten falsch sind, wird hier eine Exception geworfen.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Wenn die Authentifizierung erfolgreich war, holen wir den User und generieren einen Token.
        UserDetails user = userPort.findUserByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Benutzer nicht gefunden."));

        return jwtService.generateToken(user);
    }
}