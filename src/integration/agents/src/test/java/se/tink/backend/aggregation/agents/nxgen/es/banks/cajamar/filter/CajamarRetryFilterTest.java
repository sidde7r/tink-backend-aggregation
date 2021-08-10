package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class CajamarRetryFilterTest {
    private static final String RECONNECT_APP_RESPONSE =
            "{\"code\":\"SYS107\",\"message\":\"Ups, parece que hay un problema con la app. Por favor, sal de la app y vuelve a conectarte.\",\"errorId\":null}";
    private static final String SERVICE_TEMPORARY_UNAVAILABLE =
            "{\"code\" : \"SYS203\", \"message\" : \"Ups, hemos tenido un problema. Vuelve a intentarlo o contacta con nosotros.\"}";
    @Mock private HttpRequest httpRequest;
    @Mock private HttpResponse httpResponse;
    @Mock private Filter filter;
    private CajamarRetryFilter cajamarRetryFilter;

    @Before
    public void beforeClass() {
        this.cajamarRetryFilter = new CajamarRetryFilter(3, 1);
        cajamarRetryFilter.setNext(filter);
    }

    @Test
    public void shouldPerformThreeAttemptsWhenErrorIsAboutReconnectingApplicationHasAppeared() {
        // given
        when(filter.handle(httpRequest)).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(RECONNECT_APP_RESPONSE);

        // when
        HttpResponse result = cajamarRetryFilter.handle(httpRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBody(String.class)).isEqualTo(RECONNECT_APP_RESPONSE);
        verify(filter, times(4)).handle(httpRequest);
    }

    @Test
    public void shouldPerformThreeAttemptsWhenErrorIsAboutThatServiceIsTemporaryUnavailable() {
        // given
        when(filter.handle(httpRequest)).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn(SERVICE_TEMPORARY_UNAVAILABLE);

        // when
        HttpResponse result = cajamarRetryFilter.handle(httpRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBody(String.class)).isEqualTo(SERVICE_TEMPORARY_UNAVAILABLE);
        verify(filter, times(4)).handle(httpRequest);
    }

    @Test
    public void shouldPerformThreeAttemptsWhenTimeoutHasAppeared() {
        // given
        doThrow(new HttpClientException("connect timed out", httpRequest))
                .when(filter)
                .handle(httpRequest);

        // when
        ThrowingCallable result = () -> cajamarRetryFilter.handle(httpRequest);

        // then
        assertThatThrownBy(result).isInstanceOf(HttpClientException.class);
        verify(filter, times(4)).handle(httpRequest);
    }
}
