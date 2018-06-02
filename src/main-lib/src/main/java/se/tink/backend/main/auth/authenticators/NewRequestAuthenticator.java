package se.tink.backend.main.auth.authenticators;

import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.main.auth.DefaultAuthenticationContext;

/**
 * This interface was added in order to not have to refactor all the RequestAuthenticators now.
 *
 * The idea is to make them all return a DefaultRequestContext instead of the AuthenticatedUser that they do now.
 *
 * When that refactor has been done, we can remove interface now called RequestAuthenticators and rename this one
 * to RequestAuthenticators.
 *
 */
public interface NewRequestAuthenticator {
    DefaultAuthenticationContext authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext)
            throws IllegalAccessException, IllegalArgumentException;
}
