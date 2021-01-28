package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.com.google.common.base.Splitter;
import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

@RunWith(MockitoJUnitRunner.class)
public class N26OAuth2RedirectFetchTokenCallTest extends N26BaseApiCallTest {

    private N26OAuth2RedirectFetchTokenCall apiCall;
    private N26ProcessStateAccessor n26ProcessStateAccessor;

    @Before
    public void init() {
        N26FetchTokenParameters n26FetchTokenParameters =
                N26FetchTokenParameters.builder()
                        .baseUrl(BASE_URL)
                        .clientId(CLIENT_ID)
                        .redirectUrl(REDIRECT_URL)
                        .scope("DEDICATED_AISP")
                        .build();
        apiCall =
                new N26OAuth2RedirectFetchTokenCall(
                        agentHttpClient, n26FetchTokenParameters, new ObjectMapper());
        AgentAuthenticationProcessState agentAuthenticationProcessState =
                new AgentAuthenticationProcessState(new HashMap<>());
        n26ProcessStateAccessor =
                new N26ProcessStateAccessor(agentAuthenticationProcessState, new ObjectMapper());
    }

    @Test
    public void shouldPrepareRequestEntity() {
        RedirectFetchTokenCallAuthenticationParameters parameters =
                mock(RedirectFetchTokenCallAuthenticationParameters.class);
        AgentExtendedClientInfo clientInfo = AgentExtendedClientInfo.builder().build();

        AgentRemoteInteractionData agentRemoteInteractionData =
                mock(AgentRemoteInteractionData.class);
        when(agentRemoteInteractionData.getValue("code")).thenReturn("code");
        when(parameters.getAgentRemoteInteractionData()).thenReturn(agentRemoteInteractionData);

        N26ProcessStateData n26ProcessStateData =
                new N26ProcessStateData(URI.create(AUTH_URI), CODE_VERIFIER);
        when(parameters.getAuthenticationProcessState())
                .thenReturn(n26ProcessStateAccessor.storeN26ProcessStateData(n26ProcessStateData));

        RequestEntity<String> requestEntity = apiCall.prepareRequest(parameters, clientInfo);
        assertTrue(requestEntity.hasBody());
        assertEquals(HttpMethod.POST, requestEntity.getMethod());
        assertEquals(
                URI.create(BASE_URL + "/oauth2/token?role=DEDICATED_AISP"), requestEntity.getUrl());
        Map<String, String> bodyMap = mapRequestBodyToMap(requestEntity.getBody());
        assertEquals("code", bodyMap.get("code"));
        assertEquals(REDIRECT_URL, bodyMap.get("redirect_uri"));
        assertEquals("authorization_code", bodyMap.get("grant_type"));
        assertEquals(CODE_VERIFIER, bodyMap.get("code_verifier"));
    }

    private Map<String, String> mapRequestBodyToMap(String requestBody) {
        return new HashMap<>(
                Splitter.on('&')
                        .trimResults()
                        .withKeyValueSeparator('=')
                        .split(Objects.requireNonNull(requestBody)));
    }
}
