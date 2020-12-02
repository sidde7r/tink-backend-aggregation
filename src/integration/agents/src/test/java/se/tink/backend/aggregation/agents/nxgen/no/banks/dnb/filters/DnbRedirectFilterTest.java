package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class DnbRedirectFilterTest {

    private DnbRedirectFilter dnbRedirectFilter;

    private final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
    private HttpResponse httpResponse;

    @Before
    public void setUp() {
        Filter mockFilter = mock(Filter.class);
        dnbRedirectFilter = new DnbRedirectFilter();
        dnbRedirectFilter.setNext(mockFilter);
        httpResponse = Mockito.mock(HttpResponse.class);
        given(mockFilter.handle(httpRequest)).willReturn(httpResponse);
    }

    @SuppressWarnings("unused")
    private Object[] exceptionCombinations() {
        return new Object[] {
            new Object[] {
                302, getHeaders("asdf tekniskfeil zxcv"), BankServiceError.BANK_SIDE_FAILURE
            },
            new Object[] {
                302,
                getHeaders(" ADJASK planned_downtime_without_time ZXCMM"),
                BankServiceError.NO_BANK_SERVICE
            }
        };
    }

    @Test
    @Parameters(method = "exceptionCombinations")
    public void shouldThrowProperException(
            int statusCode, MultivaluedMap<String, String> headers, AgentError expectedError) {
        // given
        given(httpResponse.getStatus()).willReturn(statusCode);
        given(httpResponse.getHeaders()).willReturn(headers);

        // when
        Throwable thrown = catchThrowable(() -> dnbRedirectFilter.handle(httpRequest));

        // then
        assertThat(thrown).isEqualToComparingFieldByField(expectedError.exception());
    }

    @SuppressWarnings("unused")
    private Object[] noExceptionCombinations() {
        return new Object[] {
            new Object[] {302, getHeaders("")},
            new Object[] {302, getHeaders(null)},
            new Object[] {302, new MultivaluedMapImpl()},
            new Object[] {200, getHeaders("tekniskfeil")}
        };
    }

    @Test
    @Parameters(method = "noExceptionCombinations")
    public void shouldReturnResponseWithoutThrowingException(
            int statusCode, MultivaluedMap<String, String> headers) {
        // given
        given(httpResponse.getStatus()).willReturn(statusCode);
        given(httpResponse.getHeaders()).willReturn(headers);

        // when
        HttpResponse responseFromFilter = dnbRedirectFilter.handle(httpRequest);

        // then
        assertThat(responseFromFilter).isEqualTo(httpResponse);
    }

    private MultivaluedMap<String, String> getHeaders(String location) {
        MultivaluedMapImpl headers = new MultivaluedMapImpl();
        headers.add("Location", location);
        return headers;
    }
}
