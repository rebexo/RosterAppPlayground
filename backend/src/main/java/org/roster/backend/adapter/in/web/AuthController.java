package org.roster.backend.adapter.in.web;

import org.roster.backend.adapter.in.web.dto.AuthToken;
import org.roster.backend.adapter.in.web.dto.LoginRequest;
import org.roster.backend.adapter.in.web.dto.RegisterRequest;
import org.roster.backend.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    //@PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest.getUsername(), registerRequest.getPassword());
            // login user and gen token
            String token = authService.login(registerRequest.getUsername(), registerRequest.getPassword());
            //System.out.println("generated token " + token);
            return ResponseEntity.ok(new AuthToken(token));
            //return ResponseEntity.status(201).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage()); // 409 Conflict für "existiert bereits"
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            System.out.println("Generated Token: " + token); // Debug-Ausgabe
            return ResponseEntity.ok(new AuthToken(token));
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage()); // Debug-Ausgabe
            return ResponseEntity.status(401).build(); // 401 Unauthorized für falschen Login
        }
    }
}