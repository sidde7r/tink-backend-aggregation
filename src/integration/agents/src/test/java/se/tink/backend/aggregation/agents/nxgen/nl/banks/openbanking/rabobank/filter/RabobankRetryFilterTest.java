package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class RabobankRetryFilterTest {

    private static final int NUMBER_OF_RETRIES = 3;
    private static final long MS_TO_WAIT = 1;

    @Mock private Filter call;
    @Mock private HttpResponse response;

    private final RabobankRetryFilter retryFilter =
            new RabobankRetryFilter(NUMBER_OF_RETRIES, MS_TO_WAIT);

    @Before
    public void setUp() {
        configureMocks();

        retryFilter.setNext(call);
    }

    @Test
    public void shouldRetryOnNotFound() {
        // given
        givenResponse(
                404,
                "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"
                        + "<html><head>\n"
                        + "<title>404 Not Found</title>\n"
                        + "</head><body>\n"
                        + "<h1>Not Found</h1>\n"
                        + "<p>The requested URL /openapi/live/oauth2/token was not found on this server.</p>\n"
                        + "</body></html>");

        // when
        retryFilter.handle(mock(HttpRequest.class));

        // then
        then(call).should(times(4)).handle(any());
    }

    @Test
    public void shouldRetryOnTooManyRequests() {
        // given
        givenResponse(
                429,
                "{ \"httpcode\":\"429\", \"httpmessage\":\"too many requests\", \"moreinformation\":\"rate limit exceeded\" }");

        // when
        retryFilter.handle(mock(HttpRequest.class));

        // then
        then(call).should(times(4)).handle(any());
    }

    @Test
    public void shouldRetryOnUserRefreshLimitExceeded() {
        // given
        givenResponse(
                429,
                "{\"type\":\"https:\\/\\/berlingroup.com\\/error-codes\\/to-be-defined\",\"title\":\"The maximum nuber of calls for unattended requests has been exceeded for account with ID [some-id]\",\"code\":\"ACCESS_EXCEEDED\"}");

        // when
        retryFilter.handle(mock(HttpRequest.class));

        // then
        then(call).should(times(4)).handle(any());
    }

    private void configureMocks() {
        when(call.handle(any())).thenReturn(response);
    }

    private void givenResponse(int statusCode, String responseBody) {
        given(response.getStatus()).willReturn(statusCode);
        given(response.getBody(String.class)).willReturn(responseBody);
    }
}
