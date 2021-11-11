package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.filter.BarclaysRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BarclaysRetryFilterTest {
    private BarclaysRetryFilter filter;

    @Before
    public void setup() {
        filter = new BarclaysRetryFilter(4, 1500);
    }

    @Test
    public void shouldRetry() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(502);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        filter.handle(null);

        // then
        verify(nextFilter, times(5)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        filter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }
}
