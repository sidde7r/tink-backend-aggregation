package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;

public class NemidAuthenticationControllerTest {
    private NemidAuthenticationController authenticationController;

    @Before
    public void setup() {
        NemIdAuthenticator authenticator = Mockito.mock(NemIdAuthenticator.class);
        authenticationController = new NemidPasswordAuthenticationController(authenticator);
    }

    @Test(expected = LoginException.class)
    public void testThrowErrorIncorrectUserIdOrPassword() throws LoginException {
        authenticationController.throwError(
                "Incorrect user ID or password. Enter user ID and password."
                        + " Changed your password recently, perhaps?");
    }

    @Test(expected = LoginException.class)
    public void testThrowErrorIncorrectPassword() throws LoginException {
        authenticationController.throwError("Incorrect password.");
    }
}
