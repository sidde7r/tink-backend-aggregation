package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ICSRetryFilterTest {

    private ICSRetryFilter icsRetryFilter;
    private Filter nextFilter;

    @Before
    public void setUp() {
        nextFilter = mock(Filter.class);
        icsRetryFilter = new ICSRetryFilter(3, 1);
    }

    @Test
    public void shouldRetryOnce() {
        // given
        HttpResponse response = mockResponse(504, null);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        icsRetryFilter.setNext(nextFilter);
        icsRetryFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    @Test
    public void shouldRetryFourTimes() {
        // given
        HttpResponse response = mockResponse(500, null);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        icsRetryFilter.setNext(nextFilter);
        icsRetryFilter.handle(null);

        // then
        verify(nextFilter, times(4)).handle(any());
    }

    public static HttpResponse mockResponse(int statusCode, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(statusCode);
        when(mocked.getBody(String.class)).thenReturn(responseBody);
        return mocked;
    }
}
