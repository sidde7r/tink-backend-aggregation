package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankBaseHttpFilterTest {
    private SwedbankBaseHttpFilter swedbankBaseHttpFilter;

    @Before
    public void setup() {
        swedbankBaseHttpFilter = new SwedbankBaseHttpFilter("auth header");
        swedbankBaseHttpFilter.setNext(mock(Filter.class));
    }

    @Test
    public void shouldReturnAddedHeaders() {
        HttpRequest httpRequest = setupHttpRequest(HttpMethod.GET);
        assertTrue(httpRequest.getHeaders().isEmpty());

        swedbankBaseHttpFilter.handle(httpRequest);

        assertEquals(
                MediaType.APPLICATION_JSON, httpRequest.getHeaders().getFirst(HttpHeaders.ACCEPT));
        assertEquals("auth header", httpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertNull(httpRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void shouldReturnAddedHeadersAndContentHeaderWhenMethodIsPost() {
        HttpRequest httpRequest = setupHttpRequest(HttpMethod.POST);
        assertTrue(httpRequest.getHeaders().isEmpty());

        swedbankBaseHttpFilter.handle(httpRequest);

        assertEquals(
                MediaType.APPLICATION_JSON, httpRequest.getHeaders().getFirst(HttpHeaders.ACCEPT));
        assertEquals("auth header", httpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(
                MediaType.APPLICATION_JSON,
                httpRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    private HttpRequest setupHttpRequest(HttpMethod method) {
        URL url = URL.of("this is a fake URL");
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        return new HttpRequestImpl(method, url, headers, null);
    }
}
