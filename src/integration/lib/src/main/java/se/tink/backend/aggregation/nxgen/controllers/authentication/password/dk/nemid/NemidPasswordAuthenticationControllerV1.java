package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class NemidPasswordAuthenticationControllerV1 extends NemidAuthenticationControllerV1
        implements PasswordAuthenticator {

    public NemidPasswordAuthenticationControllerV1(NemIdAuthenticatorV1 authenticator) {
        super(authenticator);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        // TODO: readd NemidAuthenticationControllerV1

        try {
            doLoginWith(username, password);
            passTokenToAuthenticator();
        } finally {
            close();
        }
    }

    @Override
    void clickLogin() {}
}
