package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.utils.CurrencyConstants;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

/**
 * This test makes use of an active handelsbanken session. Log in to the Handelsbanken app and go to the
 * last https://nettbank.handelsbanken.no/secesb/rest/era/accounts request. That request will contain the values
 * for the session storage. These values are only valid as long as the session is active.
 */
public class HandelsbankenNOTest extends NextGenerationBaseAgentTest<HandelsbankenNOAgent> {

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

        for (RefreshableItem item : RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL)) {
            RefreshExecutorUtils.executeSegregatedRefresher(agent, item, testContext);
        }
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.NO.getCode();
    }

    private Agent createAgent(RefreshInformationRequest refreshInformationRequest) throws Exception {
        testContext = new AgentTestContext(refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }
}
