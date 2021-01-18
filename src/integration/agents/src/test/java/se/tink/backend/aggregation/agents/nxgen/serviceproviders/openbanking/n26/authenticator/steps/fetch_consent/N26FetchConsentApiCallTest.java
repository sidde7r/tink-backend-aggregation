package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

@RunWith(MockitoJUnitRunner.class)
public class N26FetchConsentApiCallTest extends N26BaseApiCallTest {

    private N26FetchConsentApiCall apiCall;

    @Before
    public void init() {
        apiCall = new N26FetchConsentApiCall(agentHttpClient, BASE_URL);
    }

    @Test
    public void shouldPrepareRequest() {
        // given
        N26FetchConsentParameters arg = new N26FetchConsentParameters(ACCESS_TOKEN);
        AgentExtendedClientInfo clientInfo = AgentExtendedClientInfo.builder().build();

        // when
        RequestEntity<ConsentRequest> request = apiCall.prepareRequest(arg, clientInfo);

        // then
        assertEquals(HttpMethod.POST, request.getMethod());
        assertNotNull(request.getBody());
        assertEquals(2, request.getHeaders().size());
        assertEquals(BASE_URL + "/v1/berlin-group/v1/consents", request.getUrl().toString());
    }
}
