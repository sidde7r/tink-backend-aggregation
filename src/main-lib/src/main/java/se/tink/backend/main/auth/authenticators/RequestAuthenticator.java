package se.tink.backend.main.auth.authenticators;

import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public interface RequestAuthenticator {
    AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) throws IllegalAccessException;
    HttpAuthenticationMethod method();
}
