package se.tink.backend.aggregation.agents.nxgen.uk.revolut;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class RevolutAgentTest extends NextGenerationBaseAgentTest<RevolutAgent> {
    private final String USER_ID_VALUE = "";
    private final String ACCESS_TOKEN_VALUE = "";
    private final String DEVICE_ID_VALUE = "";
    private final String USERNAME = "";

    private Credentials credentials;

    public RevolutAgentTest() {
        super(RevolutAgent.class);
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setUsername(USERNAME);
    }

    @Test
    public void testPasswordLogin() throws Exception {
        RevolutAgent agent = (RevolutAgent) createAgent(createRefreshInformationRequest(credentials));

//        PersistentStorage persistentStorage = agent.getPersistentStorage();
//        persistentStorage.put(RevolutConstants.Storage.USER_ID, USER_ID_VALUE);
//        persistentStorage.put(RevolutConstants.Storage.ACCESS_TOKEN, ACCESS_TOKEN_VALUE);
//        persistentStorage.put(RevolutConstants.Storage.DEVICE_ID, DEVICE_ID_VALUE);

        agent.login();
    }

    @Test
    public void testRefresh() throws Exception {
        RevolutAgent agent = (RevolutAgent) createAgent(createRefreshInformationRequest(credentials));
//        PersistentStorage persistentStorage = agent.getPersistentStorage();
//
//        persistentStorage.put(RevolutConstants.Storage.USER_ID, USER_ID_VALUE);
//        persistentStorage.put(RevolutConstants.Storage.ACCESS_TOKEN, ACCESS_TOKEN_VALUE);

        refresh(credentials, agent);
    }

    private void refresh(Credentials credentials, RevolutAgent agent) throws Exception {
        agent.login();

        credentials.setStatus(CredentialsStatus.UPDATING);

        agent.refresh(RefreshableItem.CHECKING_ACCOUNTS);

//        for (RefreshableItem item : RefreshableItem.values()) {
//            agent.refresh(item);
//        }

    }

    private Agent createAgent(RefreshInformationRequest refreshInformationRequest) throws Exception {
        testContext = new AgentTestContext(this, refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.UK.getCode();
    }
}



