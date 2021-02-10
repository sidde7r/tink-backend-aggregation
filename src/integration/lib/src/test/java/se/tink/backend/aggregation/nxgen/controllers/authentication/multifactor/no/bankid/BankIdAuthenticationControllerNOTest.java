package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

public class BankIdAuthenticationControllerNOTest {
    private static final String USERNAME = "username";
    private static final String MOBILENUMBER = "mobilenumber";

    private final Credentials credentials = new Credentials();
    private BankIdAuthenticationControllerNO authenticationController;
    private BankIdAuthenticatorNO authenticator;

    @Before
    public void setup() throws AuthenticationException, AuthorizationException {
        authenticator = Mockito.mock(BankIdAuthenticatorNO.class);
        SupplementalInformationController supplementalInformationController =
                Mockito.mock(SupplementalInformationController.class);
        Mockito.when(authenticator.collect()).thenReturn(BankIdStatus.DONE);
        Catalog catalog = Catalog.getCatalog("en");

        authenticationController =
                new BankIdAuthenticationControllerNO(
                        supplementalInformationController, authenticator, catalog);

        credentials.setType(CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void ensureBankIdAuthenticationController_isOfType_mobileBankId() {
        Assert.assertEquals(CredentialsTypes.MOBILE_BANKID, authenticationController.getType());
    }

    @Test(expected = NotImplementedException.class)
    public void ensureExceptionIsThrown_whenCredentials_isNotOfType_mobileBankId()
            throws AuthenticationException, AuthorizationException {
        credentials.setType(CredentialsTypes.PASSWORD);
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenNationalId_isNull()
            throws AuthenticationException, AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenNationalId_isEmpty()
            throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, "");
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenMobileumber_isNull()
            throws AuthenticationException, AuthorizationException {
        authenticationController.authenticate(credentials);
    }

    @Test(expected = LoginException.class)
    public void ensureExceptionIsThrown_whenMobileumber_isEmpty()
            throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.MOBILENUMBER, "");
        authenticationController.authenticate(credentials);
    }

    @Test
    public void ensureBankIdStatus_waiting_keepsCollectingBankIdStatus()
            throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.MOBILENUMBER, MOBILENUMBER);
        Mockito.when(authenticator.collect())
                .thenReturn(BankIdStatus.WAITING)
                .thenReturn(BankIdStatus.DONE);
        authenticationController.authenticate(credentials);

        Mockito.verify(authenticator, Mockito.times(2)).collect();
    }

    @Test
    public void ensureBankIdStatus_cancelled_isMappedTo_correctBankIdException()
            throws AuthenticationException, AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(
                BankIdStatus.CANCELLED, BankIdError.CANCELLED);
    }

    @Test
    public void ensureBankIdStatus_timeout_isMappedTo_correctBankIdException()
            throws AuthenticationException, AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(BankIdStatus.TIMEOUT, BankIdError.TIMEOUT);
    }

    @Test
    public void ensureBankIdStatus_failedUnknown_isMappedTo_correctBankIdException()
            throws AuthenticationException, AuthorizationException {
        ensureBankIdStatus_isMappedToCorrectException(
                BankIdStatus.FAILED_UNKNOWN, BankIdError.UNKNOWN);
    }

    private void ensureBankIdStatus_isMappedToCorrectException(
            BankIdStatus status, BankIdError error)
            throws AuthenticationException, AuthorizationException {
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
