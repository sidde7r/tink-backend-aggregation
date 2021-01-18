package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps;

import org.junit.Ignore;
import org.mockito.Mock;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;

@Ignore
public abstract class N26BaseApiCallTest {

    @Mock protected AgentHttpClient agentHttpClient;

    protected static final String BASE_URL = "BASE_URL";
    protected static final String CLIENT_ID = "CLIENT_ID";
    protected static final String CODE_CHALLENGE = "CODE_CHALLENGE";
    protected static final String CODE_VERIFIER = "CODE_VERIFIER";
    protected static final String REDIRECT_URL = "REDIRECT_URL";
    protected static final String STATE = "STATE";
    protected static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    protected static final String CONSENT_ID = "CONSENT_ID";
    protected static final String AUTH_URI = "AUTH_URI";
}
