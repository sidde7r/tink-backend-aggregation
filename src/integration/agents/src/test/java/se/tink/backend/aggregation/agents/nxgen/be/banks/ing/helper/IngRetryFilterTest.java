package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class IngRetryFilterTest {

    private IngRetryFilter ingRetryFilter;

    @Mock private BackOffProvider backOffProvider;

    @Before
    public void setup() {
        when(backOffProvider.calculate(anyInt())).thenReturn(0L);
        ingRetryFilter = new IngRetryFilter(2, backOffProvider);
    }

    @Test
    public void shouldRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429);
        when(nextFilter.handle(any())).thenReturn(response);

        ingRetryFilter.setNext(nextFilter);
        ingRetryFilter.handle(null);

        verify(nextFilter, times(3)).handle(any());
    }

    @Test
    public void shouldRetryOnExceptionUntilSuccess() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429);
        when(nextFilter.handle(any()))
                .thenThrow(new HttpResponseException(null, response))
                .thenReturn(mockResponse(200));

        ingRetryFilter.setNext(nextFilter);
        ingRetryFilter.handle(null);

        verify(nextFilter, times(2)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(500);
        when(nextFilter.handle(any())).thenReturn(response);

        ingRetryFilter.setNext(nextFilter);
        ingRetryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }
}
