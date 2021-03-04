package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentCombinedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class N26AutoAuthValidateConsentStepTest extends N26ValidateConsentStepBaseTest {

    @Mock private N26ValidateConsentApiCall apiCall;

    private N26AutoAuthValidateConsentStep step;

    @Before
    public void init() {
        step = new N26AutoAuthValidateConsentStep(apiCall, objectMapper);
    }

    @Test
    public void shouldDoAutoAuth() {
        AgentProceedNextStepAuthenticationRequest request =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData = preparePersistedData();

        when(request.getAuthenticationPersistedData()).thenReturn(agentAuthenticationPersistedData);

        ExternalApiCallResult<ValidateConsentCombinedResponse> apiCallResult =
                prepareSuccessfulApiCallResult(true);
        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult authenticationResult = step.execute(request);

        // then
        assertTrue(authenticationResult instanceof AgentSucceededAuthenticationResult);
    }

    @Test
    public void shouldEndUpWithSessionExpired() {
        AgentProceedNextStepAuthenticationRequest request =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData = preparePersistedData();

        when(request.getAuthenticationPersistedData()).thenReturn(agentAuthenticationPersistedData);

        ExternalApiCallResult<ValidateConsentCombinedResponse> apiCallResult =
                prepareSuccessfulApiCallResult(false);
        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult authenticationResult = step.execute(request);

        // then
        assertTrue(authenticationResult instanceof AgentFailedAuthenticationResult);
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) authenticationResult;
        assertTrue(agentFailedAuthenticationResult.getError() instanceof SessionExpiredError);
    }

    @Test
    public void shouldFailWhenBankErrorOccurs() {
        AgentProceedNextStepAuthenticationRequest request =
                mock(AgentProceedNextStepAuthenticationRequest.class);

        AgentAuthenticationPersistedData agentAuthenticationPersistedData = preparePersistedData();
        when(request.getAuthenticationPersistedData()).thenReturn(agentAuthenticationPersistedData);

        ExternalApiCallResult<ValidateConsentCombinedResponse> apiCallResult =
                mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.of(new ServerError()));
        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult authenticationResult = step.execute(request);

        // then
        assertTrue(authenticationResult instanceof AgentFailedAuthenticationResult);
    }
}
