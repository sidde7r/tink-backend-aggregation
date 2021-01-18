package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseTestStep;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class N26FetchConsentStepTest extends N26BaseTestStep {

    @Mock private N26FetchConsentApiCall n26FetchConsentApiCall;

    private N26FetchConsentStep n26FetchConsentStep;

    @Before
    public void init() {

        n26FetchConsentStep = new N26FetchConsentStep(n26FetchConsentApiCall, objectMapper);
    }

    @Test
    public void shouldExecuteSuccessfully() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        RedirectTokens redirectTokens =
                RedirectTokens.builder()
                        .accessToken(Token.builder().body("TOKEN").tokenType("token_type").build())
                        .build();
        AgentRedirectTokensAuthenticationPersistedData
                agentRedirectTokensAuthenticationPersistedData =
                        new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRedirectTokensAuthenticationPersistedData(
                                        agentAuthenticationPersistedData);

        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(
                        agentRedirectTokensAuthenticationPersistedData.storeRedirectTokens(
                                redirectTokens));

        ExternalApiCallResult<ConsentResponse> apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.empty());
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        when(consentResponse.getConsentId()).thenReturn(CONSENT_ID);
        when(apiCallResult.getResponse()).thenReturn(Optional.of(consentResponse));

        when(n26FetchConsentApiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult result =
                n26FetchConsentStep.execute(authenticationProcessRequest);

        // then
        assertTrue(result instanceof AgentProceedNextStepAuthenticationResult);
        N26ConsentAccessor n26ConsentAccessor =
                new N26ConsentAccessor(
                        authenticationProcessRequest.getAuthenticationPersistedData(),
                        objectMapper);
        assertEquals(CONSENT_ID, n26ConsentAccessor.getN26ConsentPersistentData().getConsentId());
    }

    @Test
    public void shouldFailWithoutAccessToken() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(agentAuthenticationPersistedData);

        // when
        AgentAuthenticationResult result =
                n26FetchConsentStep.execute(authenticationProcessRequest);

        assertTrue(result instanceof AgentFailedAuthenticationResult);
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) result;
        assertTrue(
                agentFailedAuthenticationResult.getError()
                        instanceof AccessTokenFetchingFailureError);
    }

    @Test
    public void shouldFailAfterApiCall() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        RedirectTokens redirectTokens =
                RedirectTokens.builder()
                        .accessToken(Token.builder().body("TOKEN").tokenType("token_type").build())
                        .build();
        AgentRedirectTokensAuthenticationPersistedData
                agentRedirectTokensAuthenticationPersistedData =
                        new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRedirectTokensAuthenticationPersistedData(
                                        agentAuthenticationPersistedData);

        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(
                        agentRedirectTokensAuthenticationPersistedData.storeRedirectTokens(
                                redirectTokens));

        ExternalApiCallResult<ConsentResponse> apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.of(new ServerError()));

        when(n26FetchConsentApiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult result =
                n26FetchConsentStep.execute(authenticationProcessRequest);

        // then
        assertTrue(result instanceof AgentFailedAuthenticationResult);
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) result;
        assertTrue(agentFailedAuthenticationResult.getError() instanceof ServerError);
    }

    @Test
    public void shouldFailWhenConsentIsNotInResponse() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        RedirectTokens redirectTokens =
                RedirectTokens.builder()
                        .accessToken(Token.builder().body("TOKEN").tokenType("token_type").build())
                        .build();
        AgentRedirectTokensAuthenticationPersistedData
                agentRedirectTokensAuthenticationPersistedData =
                        new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRedirectTokensAuthenticationPersistedData(
                                        agentAuthenticationPersistedData);

        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(
                        agentRedirectTokensAuthenticationPersistedData.storeRedirectTokens(
                                redirectTokens));

        ExternalApiCallResult<ConsentResponse> apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.empty());
        when(apiCallResult.getResponse()).thenReturn(Optional.empty());

        when(n26FetchConsentApiCall.execute(any(), any(), any())).thenReturn(apiCallResult);

        // when
        AgentAuthenticationResult result =
                n26FetchConsentStep.execute(authenticationProcessRequest);

        // then
        assertTrue(result instanceof AgentFailedAuthenticationResult);
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) result;
        assertTrue(agentFailedAuthenticationResult.getError() instanceof AuthorizationError);
    }
}
