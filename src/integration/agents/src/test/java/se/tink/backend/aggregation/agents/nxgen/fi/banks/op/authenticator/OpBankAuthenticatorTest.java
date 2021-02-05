package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.USERNAME;
import static se.tink.libraries.strings.StringUtils.hashAsUUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankMobileConfigurationsEntity;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.mocks.ResultCaptor;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OpBankAuthenticatorTest {

    @Rule public ExpectedException exception = ExpectedException.none();
    private OpAutoAuthenticator opBankAuthenticator;
    private String applicationInstanceId;
    private String username;
    private String password;
    private ResultCaptor<OpBankLoginResponseEntity> loginResultCaptor;
    private OpKeyCardAuthenticator authenticationChallenger;
    private SessionStorage sessionStorage;

    @Before
    public void setUp() throws Exception {
        applicationInstanceId = OpBankTestConfig.APPLICATION_INSTANCE_ID;
        sessionStorage = new SessionStorage();

        username = USERNAME;
        password = PASSWORD;
    }

    @Test
    public void testAutoLogin() {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
        credentials.setType(CredentialsTypes.PASSWORD);
        AgentTestContext context = new AgentTestContext(credentials);
        TinkHttpClient tinkHttpClient =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        null,
                        null,
                        context.getLogMasker(),
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
        // tinkHttpClient.setDebugOutput(true);
        // tinkHttpClient.setProxy("http://127.0.0.1:8888");

        OpBankPersistentStorage persistentStorage =
                new OpBankPersistentStorage(null, new PersistentStorage());
        persistentStorage.put(
                OpBankConstants.Authentication.APPLICATION_INSTANCE_ID, applicationInstanceId);
        OpAutoAuthenticator oaa =
                new OpAutoAuthenticator(
                        new OpBankApiClient(tinkHttpClient), persistentStorage, credentials);
        try {
            oaa.autoAuthenticate();
            oaa.authenticate(username, password);
        } catch (SessionException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void twoFactorAuthentication() throws Exception {
        applicationInstanceId = hashAsUUID("TINK-TEST");

        testAuthenticate();

        verify(authenticationChallenger, times(1)).init(username, password);
    }

    @Test
    public void succesfulLogin() throws Exception {
        testAuthenticate();

        assertTrue(loginResultCaptor.getActual().loginSingleFactor());
        verify(authenticationChallenger, never()).init(username, password);
    }

    @Test
    public void badFormatUserId() throws Exception {
        expectIncorrectCredentials();

        username = "fel";

        testAuthenticate();
    }

    @Test
    public void badFormatPassword() throws Exception {
        expectIncorrectCredentials();

        password = "fel";

        testAuthenticate();
    }

    @Test
    public void badFormatUsernameAndPassword() throws Exception {
        expectIncorrectCredentials();

        username = "fel";
        password = "fel";

        testAuthenticate();
    }

    @Test
    public void badPassword() throws Exception {
        expectIncorrectCredentials();

        password = "0000";

        testAuthenticate();
    }

    @Test
    public void badUsername() throws Exception {
        expectIncorrectCredentials();

        username = "33424324";

        testAuthenticate();
    }

    @Test
    public void badUsernameAndPassword() throws Exception {
        expectIncorrectCredentials();

        username = "33424324";
        password = "0000";

        testAuthenticate();
    }

    @Test
    public void authenticationToken() throws Exception {
        assertEquals(
                "G26346568812ec7a3d569029c9a70eb69d2446b4a",
                OpAuthenticationTokenGenerator.calculateAuthToken(
                        "287c72ce2e45d4ead20fc55ba53bafa1eef550eaae47af34ac684277c6d4feaf5d3930082f482f9231da7bb8483c5614"));
    }

    private void testAuthenticate() throws AuthenticationException, AuthorizationException {
        setupAuthenticator();

        doNothing().when(authenticationChallenger).init(username, password);

        authenticate();
    }

    private void setupAuthenticator() {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
        credentials.setType(CredentialsTypes.PASSWORD);
        AgentContext context = new AgentTestContext(credentials);

        OpBankApiClient bankClient =
                spy(
                        new OpBankApiClient(
                                new LegacyTinkHttpClient(
                                        context.getAggregatorInfo(),
                                        context.getMetricRegistry(),
                                        context.getLogOutputStream(),
                                        null,
                                        null,
                                        context.getLogMasker(),
                                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS)));
        loginResultCaptor = new ResultCaptor<>();
        doAnswer(loginResultCaptor).when(bankClient).login(any());
        doReturn(new OpBankMobileConfigurationsEntity())
                .when(bankClient)
                .enableExtendedMobileServices(
                        anyString()); // just to make sure test data is never accidentally messed up

        OpBankPersistentStorage persistentStorage =
                new OpBankPersistentStorage(credentials, new PersistentStorage());
        persistentStorage.put(
                OpBankConstants.Authentication.APPLICATION_INSTANCE_ID, applicationInstanceId);
        authenticationChallenger =
                spy(
                        new OpKeyCardAuthenticator(
                                bankClient, persistentStorage, credentials, sessionStorage));
        opBankAuthenticator =
                spy(new OpAutoAuthenticator(bankClient, persistentStorage, credentials));
    }

    private void authenticate() throws AuthenticationException, AuthorizationException {
        opBankAuthenticator.authenticate(username, password);
    }

    private void expectIncorrectCredentials() {
        exception.expect(AuthenticationException.class);
        exception.expectMessage(LoginError.INCORRECT_CREDENTIALS.exception().getMessage());
    }
}
