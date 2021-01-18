package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

@RunWith(MockitoJUnitRunner.class)
public class N26ValidateConsentApiCallTest extends N26BaseApiCallTest {

    private N26ValidateConsentApiCall apiCall;

    @Before
    public void init() {
        apiCall = new N26ValidateConsentApiCall(agentHttpClient, BASE_URL);
    }

    @Test
    public void shouldPrepareRequest() {
        N26ValidateConsentParameters parameters = mock(N26ValidateConsentParameters.class);
        when(parameters.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(parameters.getConsentId()).thenReturn(CONSENT_ID);

        RequestEntity<Void> requestEntity =
                apiCall.prepareRequest(parameters, AgentExtendedClientInfo.builder().build());

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
}
