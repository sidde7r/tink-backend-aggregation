package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class StarlingTerminatedHandshakeRetryFilterTest {

    @Mock private Filter filter;
    @Mock private HttpRequest httpRequest;
    private RuntimeException runtimeException;
    private StarlingTerminatedHandshakeRetryFilter handshakeRetryFilter;

    @Before
    public void setUp() {
        this.handshakeRetryFilter = new StarlingTerminatedHandshakeRetryFilter(3, 1500);
        this.handshakeRetryFilter.setNext(filter);
        runtimeException = new RuntimeException("Remote host terminated the handshake", null);
    }

    @Test
    public void shouldRetry3TimesWhenUsingDefaultValue() {
        // given
        when(handshakeRetryFilter.handle(httpRequest)).thenThrow(runtimeException);

        // when
        Throwable throwable = catchThrowable(() -> handshakeRetryFilter.handle(httpRequest));

        // then
        assertThat(throwable).isInstanceOf(BankServiceException.class);
        verify(filter, times(4)).handle(httpRequest);
    }

    @Test
    public void shouldRetryConnectionAndReturnResponse() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        when(handshakeRetryFilter.handle(httpRequest))
                .thenThrow(runtimeException)
                .thenReturn(httpResponse);

        // when
        HttpResponse response = handshakeRetryFilter.handle(httpRequest);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        verify(filter, times(2)).handle(httpRequest);
    }
}
