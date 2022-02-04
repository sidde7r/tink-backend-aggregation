package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.filter;

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
public class NordeaFiRetryFilterTest {

    private NordeaFiRetryFilter nordeaFiRetryFilter;

    @Before
    public void setUp() throws Exception {
        nordeaFiRetryFilter = new NordeaFiRetryFilter(3, 3000);
    }

    @Test
    public void shouldRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429);
        when(nextFilter.handle(any())).thenReturn(response);

        nordeaFiRetryFilter.setNext(nextFilter);
        nordeaFiRetryFilter.handle(null);

        verify(nextFilter, times(4)).handle(any());
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }
}
