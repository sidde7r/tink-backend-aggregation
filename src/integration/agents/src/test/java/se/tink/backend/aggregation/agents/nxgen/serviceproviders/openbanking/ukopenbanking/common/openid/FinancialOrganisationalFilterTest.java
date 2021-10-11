package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.FinancialOrganisationIdFilter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class FinancialOrganisationalFilterTest {

    private static final String ORG_ID = "DUMMY_ORG_ID";
    private FinancialOrganisationIdFilter sut;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private Filter nextFilter;

    @Before
    public void setUp() {
        sut = new FinancialOrganisationIdFilter(ORG_ID);
        httpRequest = mock(HttpRequest.class);
        httpResponse = mock(HttpResponse.class);
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldReturnSingleNewFinancialId() {
        // given
        httpRequest = setupHttpRequestWithSingleFinancialId();
        httpResponse = setupHttpResponse();
        sut.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(httpResponse);

        // when
        sut.handle(httpRequest);

        // then
        assertThat((httpRequest.getHeaders().get(X_FAPI_FINANCIAL_ID).size())).isEqualTo(1);
        assertThat(
                        (httpRequest.getHeaders().get(X_FAPI_FINANCIAL_ID).stream()
                                .findFirst()
                                .orElse(null)))
                .isEqualTo(ORG_ID);
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
