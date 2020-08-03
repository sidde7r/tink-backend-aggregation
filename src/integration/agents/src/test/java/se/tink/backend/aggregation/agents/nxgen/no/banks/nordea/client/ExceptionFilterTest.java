package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ExceptionFilterTest {

    private ExceptionFilter exceptionFilter;
    private Filter mockFilter;

    private final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);

    @Before
    public void setUp() {
        mockFilter = mock(Filter.class);
        exceptionFilter = new ExceptionFilter();
        exceptionFilter.setNext(mockFilter);
    }

    @Test
    public void shouldResultInSessionExceptionWhenErrorResponseWithInvalidGrantError() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(400);
        when(mockHttpResponse.getBody(ErrorResponse.class))
                .thenReturn(new ErrorResponse("invalid_grant", "", 400));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> exceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }
}
