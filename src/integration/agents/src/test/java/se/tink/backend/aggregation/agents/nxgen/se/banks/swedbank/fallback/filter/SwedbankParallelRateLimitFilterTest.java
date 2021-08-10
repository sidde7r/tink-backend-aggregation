package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankParallelRateLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppMessage;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankParallelRateLimitFilterTest {
    private SwedbankParallelRateLimitFilter filter;

    private static final int NUMBER_OF_RETRIES = 3;
    private static final long MS_TO_WAIT = 1000;

    @Before
    public void setup() {
        filter = new SwedbankParallelRateLimitFilter(NUMBER_OF_RETRIES, MS_TO_WAIT);
    }

    @Test
    public void shouldRetry() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429, "Reached parallel requests limit: (60)");
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        filter.handle(null);

        // then
        verify(nextFilter, times(4)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429, "Reached hour requests limit (120000)");
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        filter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int status, String text) {
        TppMessage mockTppMessage = new TppMessage("ACCESS_EXCEEDED", text, "ERROR");
        TppErrorResponse mockErrorResponse =
                new TppErrorResponse(Collections.singletonList(mockTppMessage));

        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        when(mocked.getBody(TppErrorResponse.class)).thenReturn(mockErrorResponse);

        return mocked;
    }
}
