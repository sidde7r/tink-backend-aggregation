package se.tink.backend.aggregation.nxgen.http.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.http.NoHttpResponseException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.NoHttpResponseErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NoHttpResponseErrorFilterTest {

    private Filter mockFilter;

    private NoHttpResponseErrorFilter noHttpResponseErrorFilter;

    private HttpRequest httpRequest;

    @Before
    public void setUp() {
        mockFilter = mock(Filter.class);

        noHttpResponseErrorFilter = new NoHttpResponseErrorFilter();
        noHttpResponseErrorFilter.setNext(mockFilter);

        httpRequest = new HttpRequestImpl(HttpMethod.POST, new URL("url"));
    }

    @Test
    public void shouldReportBankServiceErrorWhenFailedToRespondExceptionIsReported() {
        // given
        final ClientHandlerException clientHandlerException =
                new ClientHandlerException(new NoHttpResponseException("443 failed to respond"));

        when(mockFilter.handle(any(HttpRequest.class))).thenThrow(clientHandlerException);

        // when
        final Throwable thrown =
                catchThrowable(() -> noHttpResponseErrorFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("failed to respond");
    }

    @Test
    public void shouldRethrowExceptionWhenOtherExceptionIsReported() {
        // given
        final ClientHandlerException clientHandlerException =
                new ClientHandlerException("Other exception");

        when(mockFilter.handle(any(HttpRequest.class))).thenThrow(clientHandlerException);

        // when
        final Throwable thrown =
                catchThrowable(() -> noHttpResponseErrorFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ClientHandlerException.class)
                .hasMessage("Other exception");
    }

    @Test
    public void shouldReturnResponseWhenNoExceptionIsReported() {
        // given
        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        when(mockFilter.handle(any(HttpRequest.class))).thenReturn(mockHttpResponse);

        // when
        final HttpResponse returnedResponse = noHttpResponseErrorFilter.handle(httpRequest);

        // then
        assertThat(returnedResponse).isEqualTo(mockHttpResponse);
    }
}
