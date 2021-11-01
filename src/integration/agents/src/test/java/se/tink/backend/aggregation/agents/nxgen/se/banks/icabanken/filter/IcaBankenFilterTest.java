package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class IcaBankenFilterTest {

    @Mock Filter filter;
    @Mock HttpRequest httpRequest;
    @Mock HttpResponse httpResponse;
    @Mock ErrorResponse errorResponse;
    private IcaBankenFilter icaBankenFilter;
    @Mock ResponseStatusEntity responseStatus;

    @Before
    public void setup() {
        icaBankenFilter = new IcaBankenFilter();
        given(httpRequest.getHeaders()).willReturn(getHeaders());

        icaBankenFilter.setNext(filter);
    }

    @Test
    public void assertValidResponseContainsAllHeaders() {
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_OK);

        when(filter.handle(any())).thenReturn(httpResponse);

        icaBankenFilter.handle(httpRequest);

        assertEquals(7, httpRequest.getHeaders().size());
        assertEquals(MediaType.APPLICATION_JSON, httpRequest.getHeaders().getFirst(Headers.ACCEPT));
        assertEquals(
                Headers.VALUE_API_VERSION,
                httpRequest.getHeaders().getFirst(Headers.HEADER_API_VERSION));
        assertEquals(
                Headers.VALUE_APIKEY, httpRequest.getHeaders().getFirst(Headers.HEADER_APIKEY));
        assertEquals(
                Headers.VALUE_CLIENTAPPVERSION,
                httpRequest.getHeaders().getFirst(Headers.HEADER_CLIENTAPPVERSION));
        assertEquals(
                Headers.VALUE_CLIENT_OS,
                httpRequest.getHeaders().getFirst(Headers.HEADER_CLIENT_OS));
        assertEquals(
                Headers.VALUE_CLIENT_OS_VERSION,
                httpRequest.getHeaders().getFirst(Headers.HEADER_CLIENT_OS_VERSION));
        assertEquals(
                Headers.VALUE_CLIENT_HARDWARE,
                httpRequest.getHeaders().getFirst(Headers.HEADER_CLIENT_HARDWARE));
    }

    @Test
    public void shouldThrowBankSideFailureWhenResponseIsInternalServerError() {
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        given(httpResponse.getBody(ErrorResponse.class)).willReturn(errorResponse);
        given(errorResponse.isBanksideFailureError()).willReturn(true);
        given(errorResponse.getResponseStatus()).willReturn(responseStatus);
        given(responseStatus.getClientMessage()).willReturn("errormessage");

        when(filter.handle(any())).thenReturn(httpResponse);

        assertThatThrownBy(() -> icaBankenFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldReturnHttpResponseWhenResponseIsValid() {
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_OK);

        when(filter.handle(any())).thenReturn(httpResponse);

        assertEquals(icaBankenFilter.handle(httpRequest), httpResponse);
    }

    private MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        return headers;
    }
}
