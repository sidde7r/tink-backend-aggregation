package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.op_v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.tink.libraries.strings.StringUtils.hashAsUUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OpBankAuthenticationChallengerTest {

    @Rule public ExpectedException exception = ExpectedException.none();

    private String applicationInstanceId;
    private OpBankApiClient client;
    private OpAuthenticator authenticationChallenger;
    private PersistentStorage persistentStorage;

    @Before
    public void setUp() throws Exception {
        applicationInstanceId = hashAsUUID("TINK-TEST");

        Credentials credentials = new Credentials();
        credentials.setUserId("test user");
        this.persistentStorage = new PersistentStorage();
        OpBankPersistentStorage persistentStorage =
                new OpBankPersistentStorage(credentials, this.persistentStorage);
        client = mock(OpBankApiClient.class);
        AgentContext context = mock(AgentContext.class);
        SupplementalInformationController supplementalInformationController =
                new SupplementalInformationController(context, credentials);
        authenticationChallenger = new OpAuthenticator(client, persistentStorage, credentials);

        doReturn("{\"authenticationToken\":\"0000\"}")
                .when(context)
                .requestSupplementalInformation(any());
    }
}
