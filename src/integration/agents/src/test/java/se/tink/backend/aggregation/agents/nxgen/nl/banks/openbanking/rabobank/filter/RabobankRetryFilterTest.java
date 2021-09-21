package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class RabobankRetryFilterTest {
    private RabobankRetryFilter retryFilter;

    private static final int NUMBER_OF_RETRIES = 3;
    private static final long MS_TO_WAIT = 1000;

    @Before
    public void setup() {
        retryFilter = new RabobankRetryFilter(NUMBER_OF_RETRIES, MS_TO_WAIT);
    }

    @Test
    public void shouldRetry() {
        // given
        final String responseBody =
                "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"
                        + "<html><head>\n"
                        + "<title>404 Not Found</title>\n"
                        + "</head><body>\n"
                        + "<h1>Not Found</h1>\n"
                        + "<p>The requested URL /openapi/live/oauth2/token was not found on this server.</p>\n"
                        + "</body></html>";

        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(404, responseBody);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        retryFilter.setNext(nextFilter);
        retryFilter.handle(null);

        // then
        verify(nextFilter, times(4)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        // given
        final String responseBody =
                "{ \"httpcode\":\"429\", \"httpmessage\":\"too many requests\", \"moreinformation\":\"rate limit exceeded\" }";

        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429, responseBody);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        retryFilter.setNext(nextFilter);
        retryFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int statusCode, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(statusCode);
        when(mocked.getBody(String.class)).thenReturn(responseBody);

        return mocked;
    }
}
