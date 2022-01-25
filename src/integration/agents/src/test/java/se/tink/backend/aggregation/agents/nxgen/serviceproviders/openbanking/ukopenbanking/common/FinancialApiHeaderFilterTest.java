package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.unleash.UnleashClient;

public class FinancialApiHeaderFilterTest {

    private static final String ORG_ID = "DUMMY_ORG_ID";
    private static final String INTERACTION_ID = "dummy_interaction_id";
    private FinancialApiHeaderFilter financialApiHeaderFilter;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private Filter nextFilter;
    private UnleashClient unleashClient;

    @Before
    public void setUp() {
        AgentComponentProvider agentComponentProvider = mock(AgentComponentProvider.class);
        unleashClient = mock(UnleashClient.class);

        financialApiHeaderFilter =
                new FinancialApiHeaderFilter(ORG_ID, INTERACTION_ID, agentComponentProvider);
        httpRequest = mock(HttpRequest.class);
        httpResponse = mock(HttpResponse.class);
        nextFilter = mock(Filter.class);

        when(nextFilter.handle(any())).thenReturn(httpResponse);
        when(agentComponentProvider.getUnleashClient()).thenReturn(unleashClient);
        CompositeAgentContext agentContext = mock(CompositeAgentContext.class);
        when(agentComponentProvider.getContext()).thenReturn(agentContext);
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        when(agentComponentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);
        Credentials credentials = mock(Credentials.class);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
    }

    @Test
    public void shouldReturnSingleNewFinancialId() {
        // given
        httpRequest = setupHttpRequestWithSingleFinancialId();
        httpResponse = setupHttpResponse();
        financialApiHeaderFilter.setNext(nextFilter);
        when(unleashClient.isToggleEnabled(any())).thenReturn(false);

        // when
        financialApiHeaderFilter.handle(httpRequest);

        // then
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        assertThat(headers).hasSize(1).containsOnlyKeys(X_FAPI_FINANCIAL_ID);
        assertThat(headers.getFirst(X_FAPI_FINANCIAL_ID)).isEqualTo(ORG_ID);
    }

    @Test
    public void shouldReturnBothFinancialIdAndInteractionId() {
        // given
        httpRequest = setupHttpRequestWithSingleFinancialId();
        httpResponse = setupHttpResponse();
        financialApiHeaderFilter.setNext(nextFilter);
        when(unleashClient.isToggleEnabled(any())).thenReturn(true);

        // when
        financialApiHeaderFilter.handle(httpRequest);

        // then
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        assertThat(headers).hasSize(2).containsOnlyKeys(X_FAPI_FINANCIAL_ID, X_FAPI_INTERACTION_ID);
        assertThat(headers.getFirst(X_FAPI_FINANCIAL_ID)).isEqualTo(ORG_ID);
        assertThat(headers.getFirst(X_FAPI_INTERACTION_ID)).isEqualTo(INTERACTION_ID);
    }

    private HttpRequest setupHttpRequestWithSingleFinancialId() {
        return new HttpRequestImpl(HttpMethod.GET, new URL("any"), getHeaders(), null);
    }

    private MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle(X_FAPI_FINANCIAL_ID, ORG_ID);
        return headers;
    }

    private HttpResponse setupHttpResponse() {
        when(httpResponse.getStatus()).thenReturn(429);
        return httpResponse;
    }
}
