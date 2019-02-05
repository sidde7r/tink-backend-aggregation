package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface KeyCardAuthenticator {
    KeyCardInitValues init(String username, String password) throws AuthenticationException, AuthorizationException;
    void authenticate(String code) throws AuthenticationException, AuthorizationException;
}
