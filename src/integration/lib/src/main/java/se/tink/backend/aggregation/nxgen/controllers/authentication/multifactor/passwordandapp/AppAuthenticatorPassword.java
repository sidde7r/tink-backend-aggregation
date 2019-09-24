package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface AppAuthenticatorPassword<T> {
    T init(String username, String password) throws AuthenticationException, AuthorizationException;

    void authenticate(String otp, T initValues)
            throws AuthenticationException, AuthorizationException;
}
