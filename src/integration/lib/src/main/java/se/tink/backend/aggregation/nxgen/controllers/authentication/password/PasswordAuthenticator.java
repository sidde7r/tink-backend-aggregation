package se.tink.backend.aggregation.nxgen.controllers.authentication.password;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface PasswordAuthenticator {
    void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException;
}
