package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AutoAuthenticationControllerTest {
    private CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
    private SystemUpdater systemUpdater = Mockito.mock(SystemUpdater.class);
    private TypedAuthenticator multiFactorAuthenticator = Mockito.mock(TypedAuthenticator.class);
    private AutoAuthenticator autoAuthenticator = Mockito.mock(AutoAuthenticator.class);
    private AutoAuthenticationController autoAuthenticationController;

    private Credentials credentials = Mockito.mock(Credentials.class);

    @Before
    public void setup() {
        autoAuthenticationController =
                new AutoAuthenticationController(
                        request, systemUpdater, multiFactorAuthenticator, autoAuthenticator);

        Mockito.when(request.getCredentials()).thenReturn(credentials);
        Mockito.doCallRealMethod().when(credentials).setType(Mockito.any(CredentialsTypes.class));
        Mockito.when(credentials.getType()).thenCallRealMethod();

        Mockito.when(multiFactorAuthenticator.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);
    }

    /** Instantiation tests */
    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenCredentialsRequest_isNull() {
        new AutoAuthenticationController(
                null, systemUpdater, multiFactorAuthenticator, autoAuthenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenContext_isNull() {
        new AutoAuthenticationController(
                request, null, multiFactorAuthenticator, autoAuthenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenMultiFactorAuthenticator_isNull() {
        new AutoAuthenticationController(request, systemUpdater, null, autoAuthenticator);
    }

    @Test(expected = NullPointerException.class)
    public void ensureExceptionIsThrown_whenAutoAuthenticator_isNull() {
        new AutoAuthenticationController(request, systemUpdater, multiFactorAuthenticator, null);
    }

    /** Manual authentication */
    @Test
    public void
            ensureCredentialsStatusIsReset_beforeManualAuthentication_whenCredentialsTypeIsPassword()
                    throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.UPDATE);
        credentials.setType(CredentialsTypes.PASSWORD);
        Assert.assertNotEquals(multiFactorAuthenticator.getType(), credentials.getType());

        InOrder order = Mockito.inOrder(credentials, multiFactorAuthenticator, systemUpdater);
        order.verify(credentials).setType(CredentialsTypes.PASSWORD);

        autoAuthenticationController.authenticate(credentials);

        order.verify(credentials).setType(CredentialsTypes.MOBILE_BANKID);
        order.verify(multiFactorAuthenticator).authenticate(credentials);
        order.verify(credentials).setType(CredentialsTypes.PASSWORD);
        order.verify(systemUpdater)
                .updateCredentialsExcludingSensitiveInformation(credentials, false);
        Assert.assertEquals(CredentialsTypes.PASSWORD, credentials.getType());
    }

    @Test(expected = SessionException.class)
    public void
            ensureSessionExceptionIsThrown_whenRequestIsAutomatic_andManualAuthenticationIsRequired()
                    throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.REFRESH_INFORMATION);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        Mockito.when(request.isManual()).thenReturn(false);

        autoAuthenticationController.authenticate(credentials);
    }

    @Test
    public void ensureUpdateRequest_isForcedToAuthenticateManually()
            throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.UPDATE);
        credentials.setType(CredentialsTypes.PASSWORD);
        Assert.assertNotEquals(multiFactorAuthenticator.getType(), credentials.getType());

        InOrder order = Mockito.inOrder(credentials, multiFactorAuthenticator);
        order.verify(credentials).setType(CredentialsTypes.PASSWORD);

        autoAuthenticationController.authenticate(credentials);

        order.verify(credentials).setType(CredentialsTypes.MOBILE_BANKID);
        order.verify(multiFactorAuthenticator).authenticate(credentials);
        order.verify(credentials).setType(CredentialsTypes.PASSWORD);
        Assert.assertEquals(CredentialsTypes.PASSWORD, credentials.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void
            ensureExceptionIsThrown_whenCredentialsTypeDoesNotMatchAuthenticatorType_andRequestIsCreate()
                    throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.CREATE);
        credentials.setType(CredentialsTypes.PASSWORD);
        Assert.assertNotEquals(multiFactorAuthenticator.getType(), credentials.getType());

        autoAuthenticationController.authenticate(credentials);
    }

    @Test
    public void ensureCredentialsType_doesNotChange_whenManualAuthentication_throwsException()
            throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.CREATE);
        Mockito.doThrow(LoginError.INCORRECT_CREDENTIALS.exception())
                .when(multiFactorAuthenticator)
                .authenticate(credentials);

        try {
            autoAuthenticationController.authenticate(credentials);
            Assert.fail("Expected LoginException to be thrown");
        } catch (LoginException e) {
            Assert.assertEquals(multiFactorAuthenticator.getType(), credentials.getType());

            InOrder order = Mockito.inOrder(multiFactorAuthenticator, systemUpdater, credentials);
            order.verify(multiFactorAuthenticator).authenticate(credentials);
            order.verify(credentials, Mockito.never()).setType(CredentialsTypes.PASSWORD);
            order.verify(systemUpdater)
                    .updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    @Test(expected = SessionException.class)
    public void ensureExceptionIsThrown_whenRequestIsAutomatic_andManualAuthenticationIsRequired()
            throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.REFRESH_INFORMATION);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        Mockito.when(request.isManual()).thenReturn(false);

        autoAuthenticationController.authenticate(credentials);
    }

    /** Automatic authentication */
    @Test
    public void
            ensureExceptionIsThrownAndCredentialsTypeIsReset_whenAutoAuthenticationFails_andRequestIsAutomatic()
                    throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.REFRESH_INFORMATION);
        credentials.setType(CredentialsTypes.PASSWORD);
        Assert.assertNotEquals(multiFactorAuthenticator.getType(), credentials.getType());
        Mockito.doThrow(SessionError.SESSION_EXPIRED.exception())
                .when(autoAuthenticator)
                .autoAuthenticate();

        Mockito.when(request.isManual()).thenReturn(false);

        try {
            autoAuthenticationController.authenticate(credentials);
            Assert.fail("Expected SessionException to be thrown");
        } catch (SessionException e) {
            Assert.assertEquals(multiFactorAuthenticator.getType(), credentials.getType());

            InOrder order = Mockito.inOrder(autoAuthenticator, systemUpdater, credentials);
            order.verify(autoAuthenticator).autoAuthenticate();
            order.verify(credentials).setType(multiFactorAuthenticator.getType());
            order.verify(systemUpdater)
                    .updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    @Test
    public void
            ensureExceptionIsThrownAndCredentialsTypeIsReset_whenAutomaticAndManualAuthenticationFails()
                    throws AuthenticationException, AuthorizationException {
        prepareCredentialsRequest(CredentialsRequestType.REFRESH_INFORMATION);
        credentials.setType(CredentialsTypes.PASSWORD);
        Assert.assertNotEquals(multiFactorAuthenticator.getType(), credentials.getType());
        Mockito.doThrow(SessionError.SESSION_EXPIRED.exception())
                .when(autoAuthenticator)
                .autoAuthenticate();
        Mockito.when(request.isManual()).thenReturn(true);
        Mockito.doThrow(LoginError.INCORRECT_CREDENTIALS.exception())
                .when(multiFactorAuthenticator)
                .authenticate(credentials);

        try {
            autoAuthenticationController.authenticate(credentials);
            Assert.fail("Expected LoginException to be thrown");
        } catch (LoginException e) {
            Assert.assertEquals(multiFactorAuthenticator.getType(), credentials.getType());

            InOrder order =
                    Mockito.inOrder(
                            autoAuthenticator,
                            multiFactorAuthenticator,
                            systemUpdater,
                            credentials);
            order.verify(autoAuthenticator).autoAuthenticate();
            order.verify(multiFactorAuthenticator).authenticate(credentials);
            order.verify(credentials).setType(multiFactorAuthenticator.getType());
            order.verify(systemUpdater)
                    .updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    /** Helper methods */
    private void prepareCredentialsRequest(CredentialsRequestType type) {
        Mockito.when(request.getType()).thenReturn(type);

        switch (type) {
            case CREATE:
                Mockito.when(request.isCreate()).thenReturn(true);
                Mockito.when(request.isUpdate()).thenReturn(false);
                Mockito.when(request.isManual()).thenReturn(true);
                credentials.setType(multiFactorAuthenticator.getType());
                break;
            case UPDATE:
                Mockito.when(request.isCreate()).thenReturn(false);
                Mockito.when(request.isUpdate()).thenReturn(true);
                Mockito.when(request.isManual()).thenReturn(true);
                break;
            case REFRESH_INFORMATION:
                Mockito.when(request.isCreate()).thenReturn(false);
                Mockito.when(request.isUpdate()).thenReturn(false);
                break;
            default:
                // Nothing
                break;
        }
    }
}
