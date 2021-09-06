package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken.filters.AddBusinessFilter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AddBusinessFilterTest {

    AddBusinessFilter addBusinessFilter;
    Filter nextFilter;
    HttpRequest httpRequestAuth;
    HttpRequest httpRequestOther;
    HttpResponse httpResponse;
    String businessId = "111111";

    @Before
    public void setup() {
        addBusinessFilter = new AddBusinessFilter(businessId);
        httpRequestAuth =
                setupHttpRequest(
                        new URL("https://api.handelsbanken.com/openbanking/psd2/v1/consents"));
        httpRequestOther =
                setupHttpRequest(
                        new URL("https://api.handelsbanken.com/openbanking/psd2/v1/accounts"));
        httpResponse = setupHttpResponse();
        nextFilter = mock(Filter.class);
        addBusinessFilter.setNext(nextFilter);
    }

    @Test
    public void shouldAddBusinessIdHeaderForAuthenticationRequest() {
        when(nextFilter.handle(any())).thenReturn(httpResponse);
        addBusinessFilter.handle(httpRequestAuth);
        Assert.assertTrue(
                httpRequestAuth.getHeaders().get("PSU-Corporate-ID").contains(businessId));
        verify(nextFilter, times(1)).handle(any());
    }

    @Test
    public void shouldNotAddBusinessIdHeaderForOtherRequest() {
        when(nextFilter.handle(any())).thenReturn(httpResponse);
        addBusinessFilter.handle(httpRequestOther);
        Assert.assertNull(httpRequestOther.getHeaders().get("PSU-Corporate-ID"));
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse setupHttpResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        return httpResponse;
    }

    private HttpRequest setupHttpRequest(URL url) {
        return new HttpRequestImpl(HttpMethod.POST, url, getHeaders(), null);
    }

    private MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        return headers;
    }
}
