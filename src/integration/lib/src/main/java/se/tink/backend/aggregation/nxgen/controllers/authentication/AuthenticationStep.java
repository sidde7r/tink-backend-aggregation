package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

/**
 * Given an authentication request, provides an authentication response. A step can be followed by
 * another step, forming a linked list.
 */
@FunctionalInterface
public interface AuthenticationStep {
    AuthenticationResponse respond(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException;
}
