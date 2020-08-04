package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
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

    @Test
    @Parameters({"400, SOMETHING_MYSTERIOUS_THAT_IS_NOT_KNOWN", "500, wooot", "403, asdf"})
    public void shouldReturnHttpResponseWithoutChangesInCaseOfUnknownError(
            int statusCode, String error) {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(statusCode);
        when(mockHttpResponse.getBody(ErrorResponse.class))
                .thenReturn(new ErrorResponse(error, "", 400));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);
        // when
        HttpResponse responseAfterHandling = exceptionFilter.handle(httpRequest);

        // then
        assertThat(responseAfterHandling).isEqualTo(mockHttpResponse);
    }

    @Test
    @Parameters({"200", "201", "204", "300"})
    public void shouldReturnHttpResponseWithoutChangesInCaseOfSuccessfulResponse(int statusCode) {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(statusCode);
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);
        // when
        HttpResponse responseAfterHandling = exceptionFilter.handle(httpRequest);

        // then
        assertThat(responseAfterHandling).isEqualTo(mockHttpResponse);
    }
}
