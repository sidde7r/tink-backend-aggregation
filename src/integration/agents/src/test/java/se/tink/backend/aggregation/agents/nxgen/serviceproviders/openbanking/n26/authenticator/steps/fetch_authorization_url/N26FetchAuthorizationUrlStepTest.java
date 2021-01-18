package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26CryptoService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseTestStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class N26FetchAuthorizationUrlStepTest extends N26BaseTestStep {

    @Mock N26FetchAuthorizationUrlApiCall apiCall;

    private N26FetchAuthorizationUrlStep n26FetchAuthorizationUrlStep;

    @Before
    public void init() {
        N26CryptoService n26CryptoService = mock(N26CryptoService.class);
        when(n26CryptoService.generateCodeVerifier()).thenReturn(CODE_VERIFIER);
        when(n26CryptoService.generateCodeChallenge(CODE_VERIFIER)).thenReturn(CODE_CHALLENGE);

        n26FetchAuthorizationUrlStep =
                new N26FetchAuthorizationUrlStep(
                        apiCall, CLIENT_ID, REDIRECT_URL, n26CryptoService, objectMapper);
    }

    @Test
    public void shouldFetchRedirect() throws IOException {
        // given
        AgentProceedNextStepAuthenticationRequest request =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationProcessState processState =
                new AgentAuthenticationProcessState(new HashMap<>());

        ExternalApiCallResult<URI> externalApiCallResult = mock(ExternalApiCallResult.class);
        when(externalApiCallResult.getAgentBankApiError()).thenReturn(Optional.empty());
        when(externalApiCallResult.getResponse()).thenReturn(Optional.of(URI.create(REDIRECT_URL)));

        when(request.getAuthenticationProcessState()).thenReturn(processState);
        when(request.getAgentExtendedClientInfo())
                .thenReturn(
                        AgentExtendedClientInfo.builder()
                                .clientInfo(
                                        AgentClientInfo.builder().appId("dummyTestAppId").build())
                                .build());
        when(apiCall.execute(any(), any(), any())).thenReturn(externalApiCallResult);

        // when
        AgentAuthenticationResult result = n26FetchAuthorizationUrlStep.execute(request);

        // then
        N26ProcessStateData n26ProcessStateData =
                objectMapper.readValue(
                        processState.get("N26ProcessStateData"), N26ProcessStateData.class);
        assertEquals(CODE_VERIFIER, n26ProcessStateData.getCodeVerifier());
        assertEquals(URI.create(REDIRECT_URL), n26ProcessStateData.getAuthorizationUri());
        assertTrue(result instanceof AgentProceedNextStepAuthenticationResult);
    }

    @Test
    public void shouldFailAfterError() {
        // given
        AgentProceedNextStepAuthenticationRequest request =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationProcessState processState =
                new AgentAuthenticationProcessState(new HashMap<>());
        when(request.getAuthenticationProcessState()).thenReturn(processState);
        when(request.getAgentExtendedClientInfo())
                .thenReturn(
                        AgentExtendedClientInfo.builder()
                                .clientInfo(
                                        AgentClientInfo.builder().appId("dummyTestAppId").build())
                                .build());

        ExternalApiCallResult<URI> externalApiCallResult = mock(ExternalApiCallResult.class);
        when(externalApiCallResult.getAgentBankApiError())
                .thenReturn(Optional.of(new ServerError()));

        when(apiCall.execute(any(), any(), any())).thenReturn(externalApiCallResult);

        // when
        AgentAuthenticationResult authenticationResult =
                n26FetchAuthorizationUrlStep.execute(request);

        assertTrue(authenticationResult instanceof AgentFailedAuthenticationResult);
    }
}
