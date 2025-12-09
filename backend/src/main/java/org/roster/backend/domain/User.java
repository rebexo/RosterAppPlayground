package org.roster.backend.domain;

import org.roster.backend.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
/**
 * Repräsentiert einen Benutzer der Anwendung (z.B. einen Dienstplaner).
 * Diese Entität speichert die Anmeldeinformationen des Benutzers und dessen {@link Role}.
 * Sie implementiert das {@link UserDetails}-Interface von Spring Security,
 * um die Authentifizierung und Autorisierung zu ermöglichen.
 *
 * @author Rebecca Kassaye
 */
@Data // Lombok für Getter, Setter, etc.
@Builder // Lombok Annotation: Ermöglicht das Erstellen von User-Objekten über das Builder-Pattern
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // Name der Tabelle in der Datenbank
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der Benutzername des Users, der für die Anmeldung verwendet wird.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Das verschlüsselte Passwort des Users.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // --- Implementierung der UserDetails-Methoden für Spring Sec ---

    /**
     * Gibt die Berechtigungen (Authorities) zurück, die dem Benutzer zugewiesen sind.
     * Basierend auf der {@link #role} des Benutzers.
     *
     * @return Eine Collection von {@link GrantedAuthority}-Objekten, die die Rollen des Benutzers darstellen.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Gibt die Rolle des Benutzers als Berechtigung zurück
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        //laufen im zur Zeit nie ab
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // zur Zeit nie gesperrt
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Anmeldedaten laufen nie ab
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Konten sind immer aktiviert
        return true;
    }
}