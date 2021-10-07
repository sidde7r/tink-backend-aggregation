package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter;

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
public class KnabRetryFilterTest {

    private KnabRetryFilter knabRetryFilter;
    private Filter nextFilter;

    @Before
    public void setUp() {
        nextFilter = mock(Filter.class);
        knabRetryFilter = new KnabRetryFilter(2, 1);
    }

    @Test
    public void shouldRetryTwice() {
        // given
        HttpResponse response = FilterTestHelperUtility.mockResponse(504, null);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        knabRetryFilter.setNext(nextFilter);
        knabRetryFilter.handle(null);

        // then
        verify(nextFilter, times(3)).handle(any());
    }

    @Test
    public void shouldNotRetry() {
        // given
        HttpResponse response = FilterTestHelperUtility.mockResponse(500, null);

        // when
        when(nextFilter.handle(any())).thenReturn(response);
        knabRetryFilter.setNext(nextFilter);
        knabRetryFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }
}
