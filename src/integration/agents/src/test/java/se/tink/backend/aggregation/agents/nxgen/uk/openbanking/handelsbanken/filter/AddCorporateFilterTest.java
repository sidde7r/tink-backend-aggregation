package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.HeaderKeys.PSU_CORPORATE_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls.AUTHORIZATION;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls.TOKEN;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.filters.AddCorporateFilter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AddCorporateFilterTest {

    private AddCorporateFilter addCorporateFilter;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private Filter filter;

    @Before
    public void setup() {
        addCorporateFilter = new AddCorporateFilter();
        httpResponse = setupHttpResponse();
        filter = mock(Filter.class);
        addCorporateFilter.setNext(filter);
    }

    @Test
    public void shouldAddAdditionalCorporateHeader() {
        // given
        httpRequest = setupHttpRequest(AUTHORIZATION);

        // when
        when(filter.handle(any())).thenReturn(httpResponse);
        addCorporateFilter.handle(httpRequest);

        // then
        Assert.assertTrue(httpRequest.getHeaders().get(PSU_CORPORATE_ID).contains("UNKNOWN"));
        verify(filter, times(1)).handle(any());
    }

    @Test
    public void shouldNotAddAdditionalCorporateHeader() {
        // given
        httpRequest = setupHttpRequest(TOKEN);

        // when
        when(filter.handle(any())).thenReturn(httpResponse);
        addCorporateFilter.handle(httpRequest);

        // then
        Assert.assertNull(httpRequest.getHeaders().get(PSU_CORPORATE_ID));
        verify(filter, times(1)).handle(any());
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
        headers.putSingle("Content-Type", "application/json");
        return headers;
    }
}
