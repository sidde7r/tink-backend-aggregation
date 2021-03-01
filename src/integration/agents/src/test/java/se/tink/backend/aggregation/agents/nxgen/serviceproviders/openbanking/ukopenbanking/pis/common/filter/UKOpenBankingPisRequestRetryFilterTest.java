package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class UKOpenBankingPisRequestRetryFilterTest {

    private UKOpenBankingPisRequestRetryFilter retryFilter;
    private static final int NUMBER_OF_RETRIES = 3;
    private static final long MS_TO_WAIT = 2000;

    @Before
    public void setup() {
        retryFilter = new UKOpenBankingPisRequestRetryFilter(NUMBER_OF_RETRIES, MS_TO_WAIT);
    }

    @Test
    public void shouldRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(HttpStatus.SC_BAD_GATEWAY);
        when(nextFilter.handle(any())).thenReturn(response);

        retryFilter.setNext(nextFilter);
        retryFilter.handle(null);

        verify(nextFilter, times(4)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(HttpStatus.SC_BAD_REQUEST);
        when(nextFilter.handle(any())).thenReturn(response);

        retryFilter.setNext(nextFilter);
        retryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }
}
