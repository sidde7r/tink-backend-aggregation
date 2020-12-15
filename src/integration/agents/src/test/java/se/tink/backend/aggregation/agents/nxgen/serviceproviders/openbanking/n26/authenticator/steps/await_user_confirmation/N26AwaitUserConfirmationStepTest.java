package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.await_user_confirmation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;

@RunWith(MockitoJUnitRunner.class)
public class N26AwaitUserConfirmationStepTest {

    private N26AwaitUserConfirmationStep confirmationStep;

    @Before
    public void init() {
        confirmationStep = new N26AwaitUserConfirmationStep(new ObjectMapper());
    }

    @Test
    public void shouldDefineInteractionResult() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(mock(AgentAuthenticationPersistedData.class));
        when(authenticationProcessRequest.getAuthenticationProcessState())
                .thenReturn(mock(AgentAuthenticationProcessState.class));

        // when
        AgentUserInteractionDefinitionResult agentUserInteractionDefinitionResult =
                confirmationStep.defineInteraction(authenticationProcessRequest);
        // then
        assertFalse(
                agentUserInteractionDefinitionResult
                        .getUserInteractionDefinition()
                        .getRequiredFields()
                        .isEmpty());
        assertEquals(
                "consentConfirmationAwait",
                agentUserInteractionDefinitionResult
                        .getUserInteractionDefinition()
                        .getRequiredFields()
                        .get(0)
                        .getFieldIdentifier());
    }

    @Test
    public void shouldExecuteUnderRetryLimit() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);

        AgentAuthenticationProcessState state =
                AgentAuthenticationProcessState.of(
                        "N26ProcessStateData",
                        "{\"authorizationUri\":\"REDIRECT_URL\","
                                + "\"codeVerifier\":\"CODE_VERIFIER\","
                                + "\"consentRetryCounter\":1}");

        when(authenticationProcessRequest.getAuthenticationProcessState()).thenReturn(state);

        // when
        AgentAuthenticationResult result = confirmationStep.execute(authenticationProcessRequest);
        // then
        assertTrue(result instanceof AgentUserInteractionDefinitionResult);
    }

    @Test
    public void shouldFailExecuteOverRetryLimit() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);

        AgentAuthenticationProcessState state =
                AgentAuthenticationProcessState.of(
                        "N26ProcessStateData",
                        "{\"authorizationUri\":\"REDIRECT_URL\","
                                + "\"codeVerifier\":\"CODE_VERIFIER\","
                                + "\"consentRetryCounter\":3}");
        when(authenticationProcessRequest.getAuthenticationProcessState()).thenReturn(state);

        // when
        AgentAuthenticationResult result = confirmationStep.execute(authenticationProcessRequest);
        // then
        assertTrue(result instanceof AgentFailedAuthenticationResult);
        assertTrue(
                ((AgentFailedAuthenticationResult) result).getError()
                        instanceof ThirdPartyAppCancelledError);
    }
}
