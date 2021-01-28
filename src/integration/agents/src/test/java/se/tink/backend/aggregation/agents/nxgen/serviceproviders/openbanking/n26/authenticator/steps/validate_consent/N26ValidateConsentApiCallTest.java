package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentCombinedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class N26ValidateConsentApiCallTest extends N26BaseApiCallTest {

    private N26ValidateConsentApiCall apiCall;

    @Before
    public void init() {
        apiCall = new N26ValidateConsentApiCall(agentHttpClient, BASE_URL);
    }

    @Test
    public void shouldPrepareRequest() {
        // given
        N26ValidateConsentParameters parameters = mock(N26ValidateConsentParameters.class);
        when(parameters.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(parameters.getConsentId()).thenReturn(CONSENT_ID);

        // when
        RequestEntity<Void> requestEntity =
                apiCall.prepareRequest(parameters, AgentExtendedClientInfo.builder().build());

        // then
        assertEquals(HttpMethod.GET, requestEntity.getMethod());
        assertEquals(
                URI.create(BASE_URL + "/v1/berlin-group/v1/consents/" + CONSENT_ID),
                requestEntity.getUrl());
        assertEquals(1, requestEntity.getHeaders().size());
        assertTrue(requestEntity.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        assertEquals(
                "Bearer " + ACCESS_TOKEN,
                requestEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
    }

    @Test
    public void shouldParseSuccessfulConsentResponse() {
        // given
        ResponseEntity<String> httpResponse = givenValidHttpResponse();
        // when
        ExternalApiCallResult<ValidateConsentCombinedResponse> apiCallResult =
                apiCall.parseResponse(httpResponse);
        // then
        ValidateConsentCombinedResponse combinedResponse = apiCallResult.getResponse().get();
        assertTrue(combinedResponse.hasValidDetails());
        ConsentDetailsResponse consentDetailsResponse = combinedResponse.getValidResponse();
        assertTrue(consentDetailsResponse.isValid());
    }

    @Test
    public void shouldParseErrorConsentResponse() {
        // given
        ResponseEntity<String> httpResponse = mock(ResponseEntity.class);
        when(httpResponse.getBody())
                .thenReturn(
                        ""
                                + "{\n"
                                + "  \"status\": 401,\n"
                                + "  \"detail\": \"Invalid token\",\n"
                                + "  \"type\": \"error\",\n"
                                + "  \"userMessage\": {\n"
                                + "    \"title\": \"Login attempt expired\",\n"
                                + "    \"detail\": \"That took too long, please try again.\"\n"
                                + "  },\n"
                                + "  \"error\": \"invalid_token\",\n"
                                + "  \"error_description\": \"Invalid token\"\n"
                                + "}");
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
        // when
        ExternalApiCallResult<ValidateConsentCombinedResponse> apiCallResult =
                apiCall.parseResponse(httpResponse);
        // then
        ValidateConsentCombinedResponse combinedResponse = apiCallResult.getResponse().get();

        assertFalse(combinedResponse.hasValidDetails());
        ValidateConsentErrorResponse errorResponse = combinedResponse.getErrorResponse();
        assertTrue(errorResponse.isLoginExpired());
    }

    private ResponseEntity<String> givenValidHttpResponse() {
        ResponseEntity<String> httpResponse = mock(ResponseEntity.class);
        when(httpResponse.getBody())
                .thenReturn(
                        ""
                                + "{\n"
                                + "  \"access\": {\n"
                                + "    \"allPsd2\": \"allAccountsWithOwnerName\"\n"
                                + "  },\n"
                                + "  \"recurringIndicator\": true,\n"
                                + "  \"validUntil\": \"2021-04-19\",\n"
                                + "  \"frequencyPerDay\": 4,\n"
                                + "  \"lastActionDate\": \"2021-01-20\",\n"
                                + "  \"consentStatus\": \"valid\",\n"
                                + "  \"_links\": {\n"
                                + "    \"account\": {\n"
                                + "      \"href\": \"/v1/berlin-group/v1/accounts\"\n"
                                + "    }\n"
                                + "  }\n"
                                + "}");
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        return httpResponse;
    }
}
