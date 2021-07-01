package se.tink.backend.aggregation.nxgen.http.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public class TerminatedHandshakeRetryFilterTest {

    @Test
    public void shouldRetry3TimesWhenUsingDefaultValue() {
        Filter finalFilter = mock(Filter.class);

        TerminatedHandshakeRetryFilter filter = spy(new TerminatedHandshakeRetryFilter());

        filter.setNext(finalFilter);

        RuntimeException runtimeException =
                new RuntimeException("Remote host terminated the handshake", null);

        when(filter.getNext().handle(null)).thenThrow(runtimeException);

        try {
            filter.handle(null);
        } catch (RuntimeException e) {

            // the 4th invocation is only a check - not a retry
            verify(filter, times(4)).shouldRetry(runtimeException);
        }
    }

    @Test
    public void shouldRetryAsManyTimesAsAgentIncomingValue() {
        Filter finalFilter = mock(Filter.class);

        TerminatedHandshakeRetryFilter filter = spy(new TerminatedHandshakeRetryFilter(7, 1000));

        filter.setNext(finalFilter);

        RuntimeException runtimeException =
                new RuntimeException("Remote host terminated the handshake", null);

        when(filter.getNext().handle(null)).thenThrow(runtimeException);

        try {
            filter.handle(null);
        } catch (RuntimeException e) {

            // the 8th invocation is only a check - not a retry
            verify(filter, times(8)).shouldRetry(runtimeException);
        }
    }
}
