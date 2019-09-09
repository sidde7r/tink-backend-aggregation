package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.USERNAME;

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
import se.tink.backend.aggregation.mocks.ResultCaptor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

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
                        new TinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null));
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
