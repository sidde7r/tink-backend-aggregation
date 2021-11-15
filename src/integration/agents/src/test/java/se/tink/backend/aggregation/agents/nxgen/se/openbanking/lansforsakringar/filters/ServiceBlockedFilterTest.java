package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServiceBlockedFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.TppErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBlockedFilterTest {

    @Mock private SystemUpdater systemUpdater;
    @Mock private Credentials credentials;
    @Mock private HttpRequest httpRequest;
    @Mock private Filter filter;
    @Mock private HttpResponse response;
    @Mock private TppErrorResponse tppErrorResponse;
    private ServiceBlockedFilter serviceBlockedFilter;

    @Before
    public void setUp() {
        serviceBlockedFilter = new ServiceBlockedFilter(systemUpdater, credentials);
        serviceBlockedFilter.setNext(filter);
    }

    @Test
    public void shouldReturnBankServiceError() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertThatThrownBy(() -> serviceBlockedFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldThrowSessionErrorWhenServiceBlocked() {
        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [{\n"
                        + "\t\t\"code\": \"SERVICE_BLOCKED\",\n"
                        + "\t\t\"text\": \"Invalid customer KYC status\",\n"
                        + "\t\t\"category\": \"ERROR\"\n"
                        + "\t}]\n"
                        + "}";

        HttpResponse mockedResponse = mockResponse(403, responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        serviceBlockedFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> serviceBlockedFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Invalid customer KYC status");
    }

    @Test
    public void shouldReturnResponse() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_FORBIDDEN);
        given(response.getBody(TppErrorResponse.class)).willReturn(tppErrorResponse);
        given(tppErrorResponse.isAnyServiceBlocked()).willReturn(false);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertEquals(serviceBlockedFilter.handle(httpRequest), response);
    }

    static HttpResponse mockResponse(int status, String responseBody) {
        TppErrorResponse errorResponse = new Gson().fromJson(responseBody, TppErrorResponse.class);
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        when(mocked.getBody(TppErrorResponse.class)).thenReturn(errorResponse);
        when(mocked.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        return mocked;
    }
}
