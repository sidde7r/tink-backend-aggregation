package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface ProgressiveAuthenticator extends Authenticator {

    /**
     * An iterable collection of authentication steps, where each step maps an authentication
     * request to an authentication response. A collection of n steps implies that supplemental
     * information is requested n-1 times, where n >= 2.
     */
    Iterable<? extends AuthenticationStep> authenticationSteps(Credentials credentials)
            throws AuthenticationException, AuthorizationException;
}
