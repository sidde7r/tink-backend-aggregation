package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface SmsOtpAuthenticator<T> {
    T init(String username) throws AuthenticationException, AuthorizationException;

    void authenticate(String otp, T initValues)
            throws AuthenticationException, AuthorizationException;
}
