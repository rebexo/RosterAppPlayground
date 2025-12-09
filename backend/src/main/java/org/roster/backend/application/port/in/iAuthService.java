package org.roster.backend.application.port.in;

import org.roster.backend.domain.User;

public interface iAuthService {
    User register(String username, String password);

    String login(String username, String password);
}
