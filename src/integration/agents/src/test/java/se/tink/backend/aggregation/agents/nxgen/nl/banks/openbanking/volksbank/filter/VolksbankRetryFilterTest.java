package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class VolksbankRetryFilterTest {

    private static final int NUMBER_OF_RETRIES = 3;
    private static final long MS_TO_WAIT = 1000;

    private VolksbankRetryFilter retryFilter;

    @Before
    public void setup() {
        retryFilter = new VolksbankRetryFilter(NUMBER_OF_RETRIES, MS_TO_WAIT);
    }

    @Test
    public void shouldRetry() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(null);
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
        final String json =
                "{\n"
                        + "   \"error\":\"invalid_request\",\n"
                        + "   \"error_description\":\"The request failed due to some unknown reason\"\n"
                        + "}";

        final TokenErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(json, TokenErrorResponse.class);
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(errorResponse);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        retryFilter.setNext(nextFilter);
        retryFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    static HttpResponse mockResponse(TokenErrorResponse responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(500);
        when(mocked.getBody(TokenErrorResponse.class)).thenReturn(responseBody);
        when(mocked.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        return mocked;
    }
}
