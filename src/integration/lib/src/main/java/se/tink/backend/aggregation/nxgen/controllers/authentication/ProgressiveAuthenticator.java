package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

// TODO auth: remove extends
public interface ProgressiveAuthenticator extends Authenticator {
    AuthenticationResponse authenticate(LoadedAuthenticationRequest authenticationRequest)
            throws AuthenticationException, AuthorizationException;
}
