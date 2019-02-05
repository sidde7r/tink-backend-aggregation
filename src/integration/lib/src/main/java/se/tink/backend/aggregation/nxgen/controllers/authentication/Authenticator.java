package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.agents.rpc.Credentials;

public interface Authenticator {
    void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException;
}
