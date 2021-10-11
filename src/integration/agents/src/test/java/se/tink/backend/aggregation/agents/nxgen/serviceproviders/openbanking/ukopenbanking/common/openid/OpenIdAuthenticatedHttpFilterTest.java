package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders.X_FAPI_FINANCIAL_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.NATIONWIDE_ORG_ID;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OpenIdAuthenticatedHttpFilterTest {

    private static final String ORG_ID = "DUMMY_ORG_ID";
    private static final String EXAMPLE_FAPI_INTERACTION_ID =
            "93bac548-d2de-4546-b106-880a5018460d";
    private OpenIdAuthenticatedHttpFilter sut;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private Filter nextFilter;

    @Before
    public void setUp() {
        OAuth2Token accessToken = mock(OAuth2Token.class);
        RandomValueGeneratorImpl randomValueGenerator = new RandomValueGeneratorImpl();
        sut = new OpenIdAuthenticatedHttpFilter(accessToken, randomValueGenerator);
        httpRequest = mock(HttpRequest.class);
        httpResponse = mock(HttpResponse.class);
        nextFilter = mock(Filter.class);
    }

    @Test
    public void testNormalUkBankInteractionIdMatching() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }

    @Test
    public void testNationwideInteractionIdMismatchExclusion() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, NATIONWIDE_ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, "MIS_MATCH_ID");
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }

    @Test(expected = HttpResponseException.class)
    public void testNormalUkBankInteractionIdMismatchThrowException() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, "MIS_MATCH_ID");
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }

    @Test
    public void shouldReturnSingleNewInteractionIdWhenOneAlreadyExist() {
        // given
        httpRequest = setupHttpRequestWithSingleInteractionId();
        httpResponse = setupHttpResponse();
        sut.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(httpResponse);

        // when
        sut.handle(httpRequest);

        // then
        assertThat((httpRequest.getHeaders().get(X_FAPI_INTERACTION_ID).size())).isEqualTo(1);
        assertThat(
                        (httpRequest.getHeaders().get(X_FAPI_INTERACTION_ID).stream()
                                .findFirst()
                                .orElse(null)))
                .isNotEqualTo(EXAMPLE_FAPI_INTERACTION_ID);
    }

    @Test
    public void shouldReturnSingleNewInteractionIdWhenNoneExist() {
        // given
        httpRequest = setupHttpRequestWithoutInteractionId();
        httpResponse = setupHttpResponse();
        sut.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(httpResponse);

        // when
        sut.handle(httpRequest);

        // then
        assertThat((httpRequest.getHeaders().get(X_FAPI_INTERACTION_ID).size())).isEqualTo(1);
    }

    private HttpRequest setupHttpRequestWithoutInteractionId() {
        return new HttpRequestImpl(
                HttpMethod.GET, new URL("any"), getHeadersWithoutInteractionId(), null);
    }

    private HttpRequest setupHttpRequestWithSingleInteractionId() {
        return new HttpRequestImpl(HttpMethod.GET, new URL("any"), getHeaders(), null);
    }

    private MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle(X_FAPI_FINANCIAL_ID, ORG_ID);
        headers.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);
        return headers;
    }

    private MultivaluedMap<String, Object> getHeadersWithoutInteractionId() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle(X_FAPI_FINANCIAL_ID, ORG_ID);
        return headers;
    }

    private HttpResponse setupHttpResponse() {
        when(httpResponse.getStatus()).thenReturn(429);
        return httpResponse;
    }
}
