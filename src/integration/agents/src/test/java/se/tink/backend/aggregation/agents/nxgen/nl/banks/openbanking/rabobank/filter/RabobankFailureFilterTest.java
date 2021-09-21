package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class RabobankFailureFilterTest {

    private RabobankFailureFilter failureFilter;
    private Filter nextFilter;

    @Before
    public void setUp() {

        failureFilter = new RabobankFailureFilter();
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldThrowBankSideFailureError() {

        // given
        final String responseBody =
                "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"
                        + "<html><head>\n"
                        + "<title>404 Not Found</title>\n"
                        + "</head><body>\n"
                        + "<h1>Not Found</h1>\n"
                        + "<p>The requested URL /openapi/live/oauth2/token was not found on this server.</p>\n"
                        + "</body></html>";

        HttpResponse mockedResponse = mockResponse(404, responseBody);
        when(nextFilter.handle(any())).thenReturn(mockedResponse);

        // when
        failureFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> failureFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Code status : 404. Error body : " + responseBody);
    }

    @Test
    public void shouldNotThrowBankSideFailureError() {

        // given
        final String responseBody =
                "{ \"httpCode\":\"403\", \"httpMessage\":\"Forbidden\", \"moreInformation\":\"Forbidden\" }";

        HttpResponse response = mockResponse(403, responseBody);
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        failureFilter.setNext(nextFilter);
        failureFilter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int statusCode, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(statusCode);
        when(mocked.getBody(String.class)).thenReturn(responseBody);
        return mocked;
    }
}
