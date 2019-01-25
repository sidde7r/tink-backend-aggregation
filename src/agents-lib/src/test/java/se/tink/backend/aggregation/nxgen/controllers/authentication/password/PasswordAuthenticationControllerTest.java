package se.tink.backend.aggregation.nxgen.controllers.authentication.password;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class PasswordAuthenticationControllerTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private final Credentials credentials = new Credentials();
    private PasswordAuthenticationController authenticationController;
    private PasswordAuthenticator authenticator;

    @Before
    public void setup() {
        authenticator = Mockito.mock(PasswordAuthenticator.class);
        authenticationController = new PasswordAuthenticationController(authenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_ifPasswordAuthenticator_isNull() {
        new PasswordAuthenticationController(null);
    }

    @Test(expected = NotImplementedException.class)
    public void ensureExceptionIsThrown_whenCredentials_isNotOfType_password() throws AuthenticationException,
            AuthorizationException {
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenUsername_isNull() throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);

        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenUsername_isEmpty() throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        credentials.setField(Field.Key.USERNAME, "");

        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenPassword_isNull() throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);

        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenPassword_isEmpty() throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, "");

        authenticationController.authenticate(credentials);
    }

    @Test
    public void ensurePasswordAuthenticationController_isOfType_password() {
        Assert.assertEquals(CredentialsTypes.PASSWORD, authenticationController.getType());
    }

    @Test
    public void ensureAuthenticatorIsUsed_whenCredentials_isOfType_password() throws AuthenticationException,
            AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);

        authenticationController.authenticate(credentials);
        Mockito.verify(authenticator).authenticate(USERNAME, PASSWORD);
    }
}
