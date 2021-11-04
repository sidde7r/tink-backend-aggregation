package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.filter.BarclaysRateLimitFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BarclaysRateLimitFilterTest {
    private BarclaysRateLimitFilter filter;

    @Before
    public void setup() {
        filter = new BarclaysRateLimitFilter("barclays-provider", 500, 1500, 3);
    }

    @Test(expected = BankServiceException.class)
    public void shouldRetryAndThrowBankServiceError() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(502);
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
