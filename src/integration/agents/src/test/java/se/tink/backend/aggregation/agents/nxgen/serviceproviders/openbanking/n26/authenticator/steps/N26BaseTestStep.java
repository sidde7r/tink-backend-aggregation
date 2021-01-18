package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;

@Ignore
public abstract class N26BaseTestStep {

    protected static String CLIENT_ID = "CLIENT_ID";
    protected static String REDIRECT_URL = "REDIRECT_URL";

    protected static String CODE_VERIFIER = "CODE_VERIFIER";
    protected static String CODE_CHALLENGE = "CODE_CHALLENGE";

    protected static final String CONSENT_ID = "CONSENT_ID";

    protected AgentProceedNextStepAuthenticationRequest
            createAgentProceedNextStepAuthenticationRequest(
                    AgentAuthenticationProcessState state,
                    AgentAuthenticationPersistedData persistedData) {
        return new AgentProceedNextStepAuthenticationRequest(
                AgentAuthenticationProcessStepIdentifier.of("test_id"), state, persistedData);
    }

    protected static final ObjectMapper objectMapper = new ObjectMapper();
}
