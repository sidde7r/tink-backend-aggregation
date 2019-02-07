package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenFIConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.mocks.ResultCaptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTestConfig.DEVICE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTestConfig.DEVICE_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTestConfig.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTestConfig.USERNAME;

public class AlandsBankenAutoAuthenticatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private CrossKeyApiClient client;
    private PersistentStorage persistentStorage;
    private CrossKeyAutoAuthenticator authenticator;

    private String deviceId;
    private String deviceToken;
    private String username;
    private String password;

    @Before
    public void setUp() throws Exception {

        persistentStorage = new PersistentStorage();

        deviceId = DEVICE_ID;
        deviceToken = DEVICE_TOKEN;
        username = USERNAME;
        password = PASSWORD;
    }

    @Test
    public void canLoginWithUsernameAndPassword() throws Exception {
        setupAuthenticator();
        ResultCaptor<LoginWithTokenResponse> captor = new ResultCaptor();
        doAnswer(captor).when(client).loginWithToken(any());

        authenticator.autoAuthenticate();

        LoginWithTokenResponse actual = captor.getActual();

        assertNotNull(actual);
        assertEquals("OK", actual.getPasswordStatus());
        assertNotEquals(DEVICE_TOKEN, actual.getDeviceToken());
        assertNotEquals(DEVICE_TOKEN, persistentStorage.get(
                CrossKeyConstants.Storage.DEVICE_TOKEN));
        assertEquals(actual.getDeviceToken(), persistentStorage.get(
                CrossKeyConstants.Storage.DEVICE_TOKEN));
    }

    @Test
    public void cannotExecuteWithoutDeviceToken() throws Exception {
        expectSessionExpired();

        deviceToken = null;
        setupAuthenticator();

        authenticator.autoAuthenticate();
    }

    @Test
    public void cannotExecuteWithoutDeviceId() throws Exception {
        expectSessionExpired();

        deviceId = null;
        setupAuthenticator();

        authenticator.autoAuthenticate();
    }

    @Test
    public void cannotLoginWithUnknownUsernameAndPassword() throws Exception {
        expectIncorrectCredentials();
        username = "KALLE";
        password = "kalle";

        testAuthenticate();
    }

    @Test
    public void cannotLoginWithoutPassword() throws Exception {
        expectIncorrectCredentials();
        password = null;

        testAuthenticate();
    }

    @Test
    public void cannotLoginWithUnknownDeviceId() throws Exception {
        expectCredentialsVerificationError();
        deviceId = "unknown id";

        try {
            testAuthenticate();
        } finally {
            assertTrue(persistentStorage.isEmpty());
        }
    }

    @Test
    public void cannotLoginWithUnknownDeviceToken() throws Exception {
        expectCredentialsVerificationError();
        deviceToken = "unknown token";

        try {
            testAuthenticate();
        } finally {
            assertTrue(persistentStorage.isEmpty());
        }
    }

    private void testAuthenticate() throws AuthenticationException, AuthorizationException {
        setupAuthenticator();
        authenticator.authenticate(username, password);
    }

    private void setupAuthenticator() {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
        credentials.setType(CredentialsTypes.PASSWORD);
        client = spy(new CrossKeyApiClient(new TinkHttpClient(),
                new AlandsBankenFIConfiguration()));
        persistentStorage.put(CrossKeyConstants.Storage.DEVICE_ID, deviceId);
        persistentStorage.put(CrossKeyConstants.Storage.DEVICE_TOKEN, deviceToken);
        authenticator = new CrossKeyAutoAuthenticator(client, new CrossKeyPersistentStorage(this.persistentStorage),
                credentials);
    }

    private void expectIncorrectCredentials() {
        expectException(LoginError.INCORRECT_CREDENTIALS.exception());
    }

    private void expectCredentialsVerificationError() {
        expectException(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());
    }

    private void expectSessionExpired() {
        expectException(SessionError.SESSION_EXPIRED.exception());
    }

    private void expectException(Exception expectedException) {
        this.exception.expect(expectedException.getClass());
        this.exception.expectMessage(expectedException.getMessage());
    }

}
