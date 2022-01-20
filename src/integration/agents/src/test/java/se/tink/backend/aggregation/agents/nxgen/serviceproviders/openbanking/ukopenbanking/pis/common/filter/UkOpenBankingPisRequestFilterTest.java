package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.AuthTokenCategory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.UkPisAuthToken;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPisRequestFilterTest {
    private static final String X_IDEMPOTENCY_KEY_HEADER = "x-idempotency-key";
    private static final String DUMMY_X_FAPI_INTERACTION_ID_HEADER_VALUE =
            "00000000-0000-4000-0000-000000000000";
    private static final String DUMMY_X_IDEMPOTENCY_KEY_HEADER_VALUE = "DUMMY_X_IDEMPOTENCY_KEY";
    private static final String DUMMY_AUTHORIZATION_HEADER_VALUE = "BEARER";

    private UkOpenBankingPisRequestFilter requestFilter;
    private UkOpenBankingJwtSignatureHelper jwtSignatureHelper;
    private UkOpenBankingPaymentStorage storage;
    private RandomValueGenerator randomValueGenerator;
    private Filter nextFilter;
    private HttpResponse httpResponse;
    private MultivaluedMap<String, Object> requestHeaders = new OutBoundHeaders();
    private MultivaluedMap<String, String> responseHeaders = new MultivaluedMapImpl();

    @Before
    public void setup() {
        jwtSignatureHelper = mock(UkOpenBankingJwtSignatureHelper.class);
        storage = mock(UkOpenBankingPaymentStorage.class);
        randomValueGenerator = mock(RandomValueGenerator.class);
        nextFilter = mock(Filter.class);
        httpResponse = mock(HttpResponse.class);

        when(storage.getToken())
                .thenReturn(
                        new UkPisAuthToken(
                                OAuth2Token.createBearer("test_access", "test_refresh", 1234),
                                AuthTokenCategory.ACCESS_TOKEN));
        when(randomValueGenerator.getUUID())
                .thenReturn(UUID.fromString(DUMMY_X_FAPI_INTERACTION_ID_HEADER_VALUE));
        when(randomValueGenerator.generateRandomHexEncoded(anyInt()))
                .thenReturn(DUMMY_AUTHORIZATION_HEADER_VALUE);

        requestFilter =
                new UkOpenBankingPisRequestFilter(
                        jwtSignatureHelper, storage, randomValueGenerator);
    }

    @Test
    public void shouldHaveSingleValueForEachHeaderWhenFirstTry() {
        // given
        HttpRequest httpRequest = setupHttpRequestWithHeaderValuesWithoutBody(requestHeaders);
        mockNextFilter(200, DUMMY_X_FAPI_INTERACTION_ID_HEADER_VALUE);

        // when
        requestFilter.handle(httpRequest);

        // then
        assertSingleValuesOfHeaders(httpRequest);
    }

    @Test
    public void shouldHaveSingleValueForEachHeaderWhenRetry() {
        // given
        HttpRequest httpRequest = setupHttpRequestWithHeaderValuesWithoutBody(getRequestHeaders());
        mockNextFilter(200, DUMMY_X_FAPI_INTERACTION_ID_HEADER_VALUE);

        // when
        requestFilter.handle(httpRequest);

        // then
        assertSingleValuesOfHeaders(httpRequest);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenFapiInteractionDiffers() {
        // given
        HttpRequest httpRequest = setupHttpRequestWithHeaderValuesWithoutBody(getRequestHeaders());
        mockNextFilter(200, "00000000-6666-4000-6666-000000000000");

        // when
        Throwable throwable = catchThrowable(() -> requestFilter.handle(httpRequest));

        // then
        assertThat(throwable)
                .isInstanceOf(HttpResponseException.class)
                .hasMessage("X_FAPI_INTERACTION_ID does not match.");
    }

    private void mockNextFilter(int httpStatus, String xFapiInteractionId) {
        httpResponse = setupHttpResponseByHttpStatus(httpStatus, xFapiInteractionId);
        requestFilter.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(httpResponse);
    }

    private void assertSingleValuesOfHeaders(HttpRequest httpRequest) {
        assertThat((httpRequest.getHeaders().get(OpenIdConstants.HttpHeaders.AUTHORIZATION).size()))
                .isEqualTo(1);
        assertThat(
                        (httpRequest
                                .getHeaders()
                                .get(OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID)
                                .size()))
                .isEqualTo(1);
        assertThat((httpRequest.getHeaders().get(X_IDEMPOTENCY_KEY_HEADER).size())).isEqualTo(1);
    }

    private HttpRequest setupHttpRequestWithHeaderValuesWithoutBody(
            MultivaluedMap<String, Object> headers) {
        return new HttpRequestImpl(HttpMethod.GET, new URL("any"), headers, null);
    }

    private MultivaluedMap<String, Object> getRequestHeaders() {
        requestHeaders.putSingle(
                OpenIdConstants.HttpHeaders.AUTHORIZATION, DUMMY_AUTHORIZATION_HEADER_VALUE);
        requestHeaders.putSingle(
                OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID,
                DUMMY_X_FAPI_INTERACTION_ID_HEADER_VALUE);
        requestHeaders.putSingle(X_IDEMPOTENCY_KEY_HEADER, DUMMY_X_IDEMPOTENCY_KEY_HEADER_VALUE);
        return requestHeaders;
    }

    private MultivaluedMap<String, String> getResponseHeaders(String xFapiInteractionId) {
        responseHeaders.putSingle(
                OpenIdConstants.HttpHeaders.X_FAPI_INTERACTION_ID, xFapiInteractionId);
        return responseHeaders;
    }

    private HttpResponse setupHttpResponseByHttpStatus(int httpStatus, String xFapiInteractionId) {
        when(httpResponse.getStatus()).thenReturn(httpStatus);
        when(httpResponse.getHeaders()).thenReturn(getResponseHeaders(xFapiInteractionId));
        return httpResponse;
    }
}
