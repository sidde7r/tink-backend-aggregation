package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankServiceUnavailableFilterTest {

    private SwedbankServiceUnavailableFilter swedbankServiceUnavailableFilter;
    private Filter nextFilter;
    private HttpRequest httpRequest;

    @Before
    public void setup() {
        swedbankServiceUnavailableFilter = new SwedbankServiceUnavailableFilter();
        nextFilter = mock(Filter.class);
        swedbankServiceUnavailableFilter.setNext(nextFilter);
        httpRequest = mock(HttpRequest.class);
    }

    @Test
    public void shouldThrowBankServiceErrorIfBankServiceIsUnavailable() {
        HttpResponse response = setupHttpResponseMockError(503, SERVICE_UNAVAILABLE);

        when(nextFilter.handle(httpRequest)).thenReturn(response);
        Throwable throwable =
                catchThrowable(() -> swedbankServiceUnavailableFilter.handle(httpRequest));

        assertThat(throwable).isExactlyInstanceOf(BankServiceException.class);
        assertEquals("Cause: BankServiceError.NO_BANK_SERVICE", throwable.getLocalizedMessage());
    }

    @Test
    public void shouldReplaceHeaderValue() {
        HttpResponse response = setupHttpResponseMockError(503, ANOTHER_CODE);

        when(nextFilter.handle(httpRequest)).thenReturn(response);
        HttpResponse result = swedbankServiceUnavailableFilter.handle(httpRequest);

        assertEquals(
                MediaType.APPLICATION_JSON, result.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void shouldReturnResponseIfNotStatus503WithoutSwitchingHeader() {
        HttpResponse response = setupHttpResponseMockError(400, ANOTHER_CODE);

        when(nextFilter.handle(httpRequest)).thenReturn(response);
        HttpResponse result = swedbankServiceUnavailableFilter.handle(httpRequest);

        assertEquals(
                "will be replaced in code", result.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(400, result.getStatus());
    }

    private HttpResponse setupHttpResponseMockError(int code, String data) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.add(HttpHeaders.CONTENT_TYPE, "will be replaced in code");

        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponse spyResponse = spy(httpResponse);
        doReturn(code).when(spyResponse).getStatus();

        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(data, ErrorResponse.class);
        doReturn(errorResponse).when(spyResponse).getBody(any());
        doReturn(headers).when(spyResponse).getHeaders();

        return spyResponse;
    }

    private static final String SERVICE_UNAVAILABLE =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"SERVICE_UNAVAILABLE\","
                    + "\"message\":\"The bank service is offline; please try again later.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String ANOTHER_CODE =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"ANOTHER_CODE\","
                    + "\"message\":\"This is another error.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";
}
