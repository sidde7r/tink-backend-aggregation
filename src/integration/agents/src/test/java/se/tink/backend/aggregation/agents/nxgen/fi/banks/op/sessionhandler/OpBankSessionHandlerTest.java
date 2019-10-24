package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.USERNAME;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.mocks.ResultCaptor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.utils.Base64Masker;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

public class OpBankSessionHandlerTest {
    private OpBankApiClient bankClient;
    private SessionStorage sessionStorage;

    @Before
    public void setUp() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);

        AgentContext context = new AgentTestContext(null);
        SupplementalInformationController supplementalInformationController =
                new SupplementalInformationController(context, credentials);
        bankClient =
                new OpBankApiClient(
                        new LegacyTinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null,
                                LogMasker.builder()
                                        .addStringMaskerBuilder(
                                                new CredentialsStringMaskerBuilder(
                                                        credentials,
                                                        ImmutableList.of(
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .PASSWORD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SECRET_KEY,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SENSITIVE_PAYLOAD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .USERNAME)))
                                        .addStringMaskerBuilder(
                                                new ClientConfigurationStringMaskerBuilder(
                                                        context.getAgentConfigurationController()
                                                                .getSecretValues()))
                                        .addStringMaskerBuilder(
                                                new Base64Masker(
                                                        context.getAgentConfigurationController()
                                                                .getSecretValues()))
                                        .build(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS));
        sessionStorage = new SessionStorage();
        OpBankPersistentStorage persistentStorage =
                new OpBankPersistentStorage(credentials, new PersistentStorage());
        persistentStorage.put(
                OpBankConstants.Authentication.APPLICATION_INSTANCE_ID,
                OpBankTestConfig.APPLICATION_INSTANCE_ID);
        OpAutoAuthenticator opBankAuthenticator =
                new OpAutoAuthenticator(bankClient, persistentStorage, credentials, sessionStorage);
        opBankAuthenticator.authenticate(USERNAME, PASSWORD);
    }

    @Test
    public void refreshSessionSucceeds() throws Exception {
        bankClient = Mockito.spy(bankClient);

        ResultCaptor<OpBankResponseEntity> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(bankClient).refreshSession();

        OpBankSessionHandler sessionHandler = new OpBankSessionHandler(bankClient);
        sessionHandler.keepAlive();

        assertTrue(resultCaptor.getActual().isSuccess());
    }

    @Test(expected = SessionException.class)
    public void refreshSessionFails() throws Exception {

        OpBankSessionHandler sessionHandler = new OpBankSessionHandler(bankClient);
        sessionHandler.logout();
        sessionHandler.keepAlive();
    }
}
