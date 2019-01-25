package se.tink.backend.aggregation.nxgen.controllers.authentication;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;

public class TypedAuthenticationControllerTest {
    private final Credentials credentials = new Credentials();
    private BankIdAuthenticationController bankIdAuthenticationController;
    private PasswordAuthenticationController passwordAuthenticationController;
    private TypedAuthenticationController authenticationController;

    @Before
    public void setup() {
        bankIdAuthenticationController = Mockito.mock(BankIdAuthenticationController.class);
        Mockito.when(bankIdAuthenticationController.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);

        passwordAuthenticationController = Mockito.mock(PasswordAuthenticationController.class);
        Mockito.when(passwordAuthenticationController.getType()).thenReturn(CredentialsTypes.PASSWORD);

        authenticationController = new TypedAuthenticationController(bankIdAuthenticationController,
                passwordAuthenticationController);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("ConstantConditions")
    public void ensureExceptionIsThrown_whenInjectedAuthenticator_isNull() {
        BankIdAuthenticationController bankIdAuthenticationController = null;
        new TypedAuthenticationController(bankIdAuthenticationController);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenCredentialsType_isAbsentOnTypedAuthenticator() {
        TypedAuthenticator typedAuthenticator = Mockito.mock(TypedAuthenticator.class);
        Mockito.when(typedAuthenticator.getType()).thenReturn(null);

        authenticationController = new TypedAuthenticationController(typedAuthenticator);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenNoAuthenticators_arePresent() {
        new TypedAuthenticationController();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenMultipleAuthenticators_ofSameType_isUsed() {
        TypedAuthenticator bankIdAuthenticationController2 = Mockito.mock(TypedAuthenticator.class);
        Mockito.when(bankIdAuthenticationController2.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);

        new TypedAuthenticationController(passwordAuthenticationController, bankIdAuthenticationController,
                bankIdAuthenticationController2);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenCredentials_isOfType_null() throws AuthenticationException,
            AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = NotImplementedException.class)
    public void ensureExceptionIsThrown_ifAuthenticator_ofSameTypeAsCredential_doesNotExist() throws AuthenticationException,
            AuthorizationException {
        credentials.setType(CredentialsTypes.KEYFOB);

        authenticationController.authenticate(credentials);
    }

    @Test
    public void ensureCorrectAuthenticator_isSelected() throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        authenticationController.authenticate(credentials);
        Mockito.verify(bankIdAuthenticationController).authenticate(Mockito.any(Credentials.class));
    }
}
