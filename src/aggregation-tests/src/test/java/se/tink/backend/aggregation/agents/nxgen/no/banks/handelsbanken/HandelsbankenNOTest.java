package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import org.junit.Test;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.CurrencyConstants;

/**
 * This test makes use of an active handelsbanken session. Log in to the Handelsbanken app and go to the
 * last https://nettbank.handelsbanken.no/secesb/rest/era/accounts request. That request will contain the values
 * for the session storage. These values are only valid as long as the session is active.
 */
public class HandelsbankenNOTest extends NextGenerationAgentTest<HandelsbankenNOAgent> {

    public HandelsbankenNOTest() {
        super(HandelsbankenNOAgent.class);
    }

    private final String EVRY_TOKEN_KEY = "accessToken";
    private final String NONCE_KEY = "SECESB_NONCE";
    private final String SESSION_STAMP_FIELD_KEY = "SECESB_SESSION_STAMP";
    private final String SESSION_STAMP_KEY = "SECESB_SESSION_STAMP_VALUE";

    // copy from account request 'X-EVRY-CLIENT-ACCESSTOKEN' field, it's a really long string (don't forget the periods!)
    private final String EVRY_TOKEN_VALUE = "";

    // copy from account request, in cookie find the only 'SECESB_NONCE' field, only the value
    private final String NONCE_VALUE = "";

    // copy from account request, in cookie find the first that starts with SECESB_SESSION_STAMP
    // copy the whole string including the whole 'STAMP' text!
    private final String SESSION_STAMP_FIELD_VALUE = "SECESB_SESSION_STAMP-...";

    // the value after '=' of previous field, usually in a '1-1' format
    private final String SESSION_STAMP_VALUE = "";

    // username of user to execute test for
    private final String USERNAME = "";

    @Test
    public void testAccount() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setStatus(CredentialsStatus.UPDATING);
        credentials.setUsername(USERNAME);
        Agent agent = createAgent(createRefreshInformationRequest(credentials));
        HandelsbankenNOAgent hbAgent = (HandelsbankenNOAgent) agent;
        hbAgent.populateSessionStorage(EVRY_TOKEN_KEY, EVRY_TOKEN_VALUE);
        hbAgent.populateSessionStorage(NONCE_KEY, NONCE_VALUE);
        hbAgent.populateSessionStorage(SESSION_STAMP_FIELD_KEY, SESSION_STAMP_FIELD_VALUE);
        hbAgent.populateSessionStorage(SESSION_STAMP_KEY, SESSION_STAMP_VALUE);

        RefreshableItemExecutor refreshExecutor = (RefreshableItemExecutor) agent;
        for (RefreshableItem item : RefreshableItem.values()) {
            refreshExecutor.refresh(item);
        }
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.NO.getCode();
    }

    private Agent createAgent(RefreshInformationRequest refreshInformationRequest) throws Exception {
        testContext = new AgentTestContext(this, refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }
}
