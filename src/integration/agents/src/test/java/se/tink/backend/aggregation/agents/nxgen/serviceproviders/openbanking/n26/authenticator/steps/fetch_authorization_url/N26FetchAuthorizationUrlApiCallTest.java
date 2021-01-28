package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpHeaders;
import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class N26FetchAuthorizationUrlApiCallTest extends N26BaseApiCallTest {

    private N26FetchAuthorizationUrlApiCall n26FetchAuthorizationUrlApiCall;

    @Mock N26FetchAuthorizationUrlApiCallParameters parameters;

    @Before
    public void init() {
        n26FetchAuthorizationUrlApiCall =
                new N26FetchAuthorizationUrlApiCall(
                        agentHttpClient,
                        N26FetchAuthorizationUrlApiParameters.builder()
                                .baseUrl(BASE_URL)
                                .scope("DEDICATED_AISP")
                                .build());
    }

    @Test
    public void shouldPrepareRequest() {
        // given
        when(parameters.getClientId()).thenReturn(CLIENT_ID);
        when(parameters.getCodeChallenge()).thenReturn(CODE_CHALLENGE);
        when(parameters.getRedirectUri()).thenReturn(REDIRECT_URL);
        when(parameters.getState()).thenReturn(STATE);

        AgentExtendedClientInfo clientInfo = AgentExtendedClientInfo.builder().build();

        // when
        RequestEntity<String> requestEntity =
                n26FetchAuthorizationUrlApiCall.prepareRequest(parameters, clientInfo);

        // then
        assertEquals(HttpMethod.GET, requestEntity.getMethod());
        assertEquals(BASE_URL + Url.AUTHORIZE, requestEntity.getUrl().getPath());
        assertEquals(
                "BASE_URL/oauth2/authorize?client_id=CLIENT_ID&scope=DEDICATED_AISP&code_challenge=CODE_CHALLENGE&redirect_uri=REDIRECT_URL&state=STATE&response_type=CODE",
                requestEntity.getUrl().toString());
    }

    @Test
    public void shouldHandleRedirect() {
        // given
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        HttpHeaders httpHeaders = mock(HttpHeaders.class);

        URI responseURI = URI.create("REDIRECT_URI");
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.TEMPORARY_REDIRECT);
        when(httpHeaders.getLocation()).thenReturn(responseURI);
        when(responseEntity.getHeaders()).thenReturn(httpHeaders);

        // when
        ExternalApiCallResult<URI> apiCallResult =
                n26FetchAuthorizationUrlApiCall.parseResponse(responseEntity);

        // then
        assertTrue(apiCallResult.getResponse().isPresent());
        assertEquals(responseURI, apiCallResult.getResponse().get());
    }

    @Test
    public void shouldReturnBankError() {
        // given
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        // when
        ExternalApiCallResult<URI> apiCallResult =
                n26FetchAuthorizationUrlApiCall.parseResponse(responseEntity);

        // then
        assertTrue(apiCallResult.getAgentBankApiError().isPresent());
        assertEquals(ServerError.class, apiCallResult.getAgentBankApiError().get().getClass());
    }

    @Test
    public void shouldReturnInvalidRequestError() {
        // given
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        // when
        ExternalApiCallResult<URI> apiCallResult =
                n26FetchAuthorizationUrlApiCall.parseResponse(responseEntity);

        // then
        assertTrue(apiCallResult.getAgentBankApiError().isPresent());
        assertEquals(
                InvalidRequestError.class, apiCallResult.getAgentBankApiError().get().getClass());
    }
}
