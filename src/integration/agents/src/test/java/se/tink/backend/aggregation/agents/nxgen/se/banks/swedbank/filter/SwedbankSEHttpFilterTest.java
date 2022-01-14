package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankSEHttpFilterTest {

    private SwedbankSeHttpFilter swedbankSeHttpFilter;
    private Filter nextFilter;
    private static final String ORG_ID = "DUMMY_ORG_ID";

    @Before
    public void setup() {
        swedbankSeHttpFilter = new SwedbankSeHttpFilter("UserAgent");
        nextFilter = mock(Filter.class);
        swedbankSeHttpFilter.setNext(nextFilter);
    }

    @Test
    public void shouldAddHeadersToRequestIfTransactionUrl() {
        HttpRequest httpRequest =
                setupHttpRequestWithoutInteractionId("/v5/engagement/transactions");
        assertEquals(1, httpRequest.getHeaders().size());

        swedbankSeHttpFilter.handle(httpRequest);

        assertEquals(4, httpRequest.getHeaders().size());
    }

    @Test
    public void shouldThrowBankServiceErrorWhenUrlIsNotTransaction() {
        HttpResponse httpResponse = setupHttpResponse();
        HttpRequest httpRequest = setupHttpRequestWithoutInteractionId("random url");
        when(nextFilter.handle(httpRequest)).thenReturn(httpResponse);

        Throwable throwable = catchThrowable(() -> swedbankSeHttpFilter.handle(httpRequest));

        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: 500, body: this is an error message");
    }

    @Test
    public void shouldThrowBankServiceErrorWhenEndpointNotAllowedException() {
        HttpResponse httpResponse = setupHttpEndpointResponse();
        HttpRequest httpRequest = setupHttpRequestWithoutInteractionId("random url");
        when(nextFilter.handle(httpRequest)).thenReturn(httpResponse);

        Throwable throwable = catchThrowable(() -> swedbankSeHttpFilter.handle(httpRequest));

        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage(
                        "Http status: 401, body: {\"tppmessages\" : [{\"category\" : \"error\",\n"
                                + "\"code\" : \"service_blocked\",\n"
                                + "\"text\" : \"endpoint not allowed\"}]}");
    }

    private HttpResponse setupHttpResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(httpResponse.getBody(String.class)).thenReturn("this is an error message");
        return httpResponse;
    }

    private HttpResponse setupHttpEndpointResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
        when(httpResponse.getBody(String.class))
                .thenReturn(
                        "{\"tppMessages\" : [{\"category\" : \"ERROR\",\n"
                                + "\"code\" : \"SERVICE_BLOCKED\",\n"
                                + "\"text\" : \"Endpoint not allowed\"}]}");
        return httpResponse;
    }

    private HttpRequest setupHttpRequestWithoutInteractionId(String url) {
        return new HttpRequestImpl(
                HttpMethod.GET, new URL(url), getHeadersWithoutInteractionId(), null);
    }

    private MultivaluedMap<String, Object> getHeadersWithoutInteractionId() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle("key", ORG_ID);
        return headers;
    }
}
