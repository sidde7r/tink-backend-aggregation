package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.no.BankIdErrorNO;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class BankIdAuthenticationControllerNOTest {
    private static final String USERNAME = "username";
    private static final String MOBILENUMBER = "mobilenumber";

    private final Credentials credentials = new Credentials();
    private BankIdAuthenticationControllerNO authenticationController;
    private BankIdAuthenticatorNO authenticator;
    private AgentContext context;

    @Before
    public void setup() throws AuthenticationException, AuthorizationException {
        authenticator = Mockito.mock(BankIdAuthenticatorNO.class);
        context = Mockito.mock(AgentContext.class);
        Mockito.when(authenticator.collect()).thenReturn(BankIdStatus.DONE);

        authenticationController = new BankIdAuthenticationControllerNO(context, authenticator);

        credentials.setType(CredentialsTypes.MOBILE_BANKID);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenBankIdAuthenticator_isNull() {
        new BankIdAuthenticationControllerNO(context, null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new BankIdAuthenticationControllerNO(null, authenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenBothContextAndBankIdAuthenticator_isNull() {
        new BankIdAuthenticationControllerNO(null, null);
    }

    @Test
    public void ensureBankIdAuthenticationController_isOfType_mobileBankId() {
        Assert.assertEquals(CredentialsTypes.MOBILE_BANKID, authenticationController.getType());
    }

    @Test(expected = NotImplementedException.class)
    public void ensureExceptionIsThrown_whenCredentials_isNotOfType_mobileBankId() throws AuthenticationException,
            AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenNationalId_isNull() throws AuthenticationException, AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenNationalId_isEmpty() throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, "");
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenMobileumber_isNull() throws AuthenticationException, AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenMobileumber_isEmpty() throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.MOBILENUMBER, "");
        authenticationController.authenticate(credentials);
    }

    @Test
    public void ensureBankIdStatus_waiting_keepsCollectingBankIdStatus() throws AuthenticationException,
            AuthorizationException {
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.MOBILENUMBER, MOBILENUMBER);
        Mockito.when(authenticator.collect()).thenReturn(BankIdStatus.WAITING)
                .thenReturn(BankIdStatus.DONE);
        authenticationController.authenticate(credentials);

        Mockito.verify(authenticator, Mockito.times(2)).collect();
    }

    @Test
    public void ensureBankIdStatus_cancelled_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.CANCELLED, BankIdErrorNO.CANCELLED);
    }

    @Test
    public void ensureBankIdStatus_timeout_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.TIMEOUT, BankIdErrorNO.TIMEOUT);
    }

    @Test
    public void ensureBankIdStatus_failedUnknown_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.FAILED_UNKNOWN, BankIdErrorNO.UNKNOWN);
    }

    private void ensureBankIdStatus_isMappedToCorrectException(BankIdStatus status, BankIdErrorNO error) throws
            AuthenticationException, AuthorizationException {
        try {
            credentials.setField(Field.Key.USERNAME, USERNAME);
            credentials.setField(Field.Key.MOBILENUMBER, MOBILENUMBER);
            Mockito.when(authenticator.collect()).thenReturn(status);

            authenticationController.authenticate(credentials);
            Assert.fail("Expected collect() to throw a BankIdException");
        } catch (BankIdException e) {
            Assert.assertEquals(e.getError(), BankIdError.valueOf(error.name()));
        }
    }
}
