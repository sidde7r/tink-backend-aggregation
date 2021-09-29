package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filters;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.LansforsakringarRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class LansforsakringarRetryFilterTest {

    private LansforsakringarRetryFilter lansforsakringarRetryFilter;
    @Mock private HttpRequest httpRequest;
    @Mock private HttpResponse httpResponse;
    @Mock private Filter filter;

    @Before
    public void setup() {
        lansforsakringarRetryFilter = new LansforsakringarRetryFilter(3, 1);
        lansforsakringarRetryFilter.setNext(filter);
    }

    @Test
    public void shouldRetryOnce() {
        // when
        when(filter.handle(httpRequest)).thenReturn(httpResponse);
        lansforsakringarRetryFilter.handle(httpRequest);

        // then
        verify(filter, times(1)).handle(httpRequest);
    }
}
