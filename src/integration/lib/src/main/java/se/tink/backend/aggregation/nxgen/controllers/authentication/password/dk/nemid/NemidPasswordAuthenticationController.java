package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class NemidPasswordAuthenticationController extends NemidAuthenticationController
        implements PasswordAuthenticator {

    public NemidPasswordAuthenticationController(NemIdAuthenticator authenticator) {
        super(authenticator);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            doLoginWith(username, password);
            passTokenToAuthenticator();
        } finally {
            close();
        }
    }

    @Override
    void clickLogin() {
        clickButton(SUBMIT_BUTTON);
    }
}
