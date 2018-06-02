package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class BankIdAuthenticationControllerTest {
    private static final String REFERENCE = "reference";
    private static final String USERNAME = "username";

    private final Credentials credentials = new Credentials();
    private BankIdAuthenticationController authenticationController;
    private BankIdAuthenticator authenticator;
    private AgentContext context;

    @Before
    public void setup() throws AuthenticationException, AuthorizationException {
        context = Mockito.mock(AgentContext.class);
        authenticator = Mockito.mock(BankIdAuthenticator.class);
        Mockito.when(authenticator.init(Mockito.anyString())).thenReturn(REFERENCE);
        Mockito.when(authenticator.collect(REFERENCE)).thenReturn(BankIdStatus.DONE);

        authenticationController = new BankIdAuthenticationController(context, authenticator);

        credentials.setType(CredentialsTypes.MOBILE_BANKID);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenBankIdAuthenticator_isNull() {
        new BankIdAuthenticationController(context, null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new BankIdAuthenticationController(null, authenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenBothContextAndBankIdAuthenticator_isNull() {
        new BankIdAuthenticationController(null, null);
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
    public void ensureExceptionIsThrown_whenSsn_isNull() throws AuthenticationException, AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenSsn_isEmpty() throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, "");
        authenticationController.authenticate(credentials);
    }

    @Test
    public void ensureBankId_isOpened_betweenInitAndPoll() throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, USERNAME);
        authenticationController.authenticate(credentials);

        InOrder order = Mockito.inOrder(authenticator, context);
        order.verify(authenticator).init(USERNAME);
        order.verify(context).openBankId(null, false);
        order.verify(authenticator).collect(REFERENCE);
    }

    @Test
    public void ensureBankIdStatus_waiting_keepsCollectingBankIdStatus() throws AuthenticationException,
            AuthorizationException {
        credentials.setField(Field.Key.USERNAME, USERNAME);
        Mockito.when(authenticator.collect(REFERENCE)).thenReturn(BankIdStatus.WAITING)
                .thenReturn(BankIdStatus.DONE);
        authenticationController.authenticate(credentials);

        Mockito.verify(authenticator, Mockito.times(2)).collect(REFERENCE);
    }

    @Test
    public void ensureBankIdStatus_cancelled_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.CANCELLED, BankIdError.CANCELLED);
    }

    @Test
    public void ensureBankIdStatus_noClient_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.NO_CLIENT, BankIdError.NO_CLIENT);
    }

    @Test
    public void ensureBankIdStatus_timeout_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.TIMEOUT, BankIdError.TIMEOUT);
    }

    @Test
    public void ensureBankIdStatus_interrupted_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.INTERRUPTED, BankIdError.INTERRUPTED);
    }

    @Test
    public void ensureBankIdStatus_failedUnknown_isMappedTo_correctBankIdException() throws AuthenticationException,
            AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.FAILED_UNKNOWN, BankIdError.UNKNOWN);
    }

    private void ensureBankIdStatus_isMappedToCorrectException(BankIdStatus status, BankIdError error) throws
            AuthenticationException, AuthorizationException {
        try {
            credentials.setField(Field.Key.USERNAME, USERNAME);
            Mockito.when(authenticator.collect(REFERENCE)).thenReturn(status);

            authenticationController.authenticate(credentials);
            Assert.fail("Expected collect() to throw a BankIdException");
        } catch (BankIdException e) {
            Assert.assertEquals(e.getError(), error);
        }
    }
}
