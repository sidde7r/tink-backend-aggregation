package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class TriodosSessionErrorFilterTest {

    private TriodosSessionErrorFilter sessionErrorFilter;
    private Filter nextFilter;

    @Before
    public void setUp() {

        sessionErrorFilter = new TriodosSessionErrorFilter();
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldThrowSessionErrorWhenConsentInvalid() {

        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"text\": \"Operation not possible on consent dummyConsentId with status expired\",\n"
                        + "\t\t\t\"code\": \"CONSENT_INVALID\",\n"
                        + "\t\t\t\"category\": \"ERROR\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        HttpResponse mockedResponse = mockResponse(responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        sessionErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> sessionErrorFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: Consent invalid or expired");
    }

    @Test
    public void shouldThrowSessionErrorWhenTokenInvalid() {

        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"text\": \"Access token dummyTokenId is invalid or expired\",\n"
                        + "\t\t\t\"code\": \"TOKEN_INVALID\",\n"
                        + "\t\t\t\"category\": \"ERROR\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        HttpResponse mockedResponse = mockResponse(responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        sessionErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> sessionErrorFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: Token invalid or expired");
    }

    @Test
    public void shouldNotThrowSessionError() {

        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"text\": \"The daily limit of 4 unattended TRANSACTIONS requests for product 4ee2b788-c989-4037-925f-1847a5413236 has been reached\",\n"
                        + "\t\t\t\"code\": \"ACCESS_EXCEEDED\",\n"
                        + "\t\t\t\"category\": \"ERROR\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        HttpResponse mockedResponse = mockResponse(responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        sessionErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> sessionErrorFilter.handle(null));

        // then
        assertThat(t).isInstanceOf(BankServiceException.class);
    }

    static HttpResponse mockResponse(String responseBody) {
        ErrorResponse errorResponse = new Gson().fromJson(responseBody, ErrorResponse.class);
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(mocked.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        return mocked;
    }
}
