package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.RetryFilterValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc.ImaginBankErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ImaginBankRetryFilterTest {
    private ImaginBankRetryFilter imaginBankRetryFilter;

    @Before
    public void setup() {
        imaginBankRetryFilter =
                new ImaginBankRetryFilter(
                        RetryFilterValues.MAX_ATTEMPTS, RetryFilterValues.RETRY_SLEEP_MILLISECONDS);
    }

    @Test
    public void shouldRetry() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response =
                setupHttpClientMockFor409StatusByErrorCode(ErrorCode.TEMPORARY_PROBLEM);
        when(nextFilter.handle(any())).thenReturn(response);

        imaginBankRetryFilter.setNext(nextFilter);
        imaginBankRetryFilter.handle(null);

        verify(nextFilter, times(RetryFilterValues.MAX_ATTEMPTS + 1)).handle(any());
    }

    @Test
    public void shouldNotRetryForDifferentErrorCode() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = setupHttpClientMockFor409StatusByErrorCode("0000");
        when(nextFilter.handle(any())).thenReturn(response);

        imaginBankRetryFilter.setNext(nextFilter);
        imaginBankRetryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    @Test
    public void shouldNotRetryForSuccessfulResponse() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = setupHttpClientMockForSuccessfulResponse();
        when(nextFilter.handle(any())).thenReturn(response);

        imaginBankRetryFilter.setNext(nextFilter);
        imaginBankRetryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse setupHttpClientMockFor409StatusByErrorCode(String errorCode) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(409);
        when(httpResponse.getBody(ImaginBankErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"codigo\":\"" + errorCode + "\",\"mensaje\":\"ERROR\"}",
                                ImaginBankErrorResponse.class));
        return httpResponse;
    }

    private HttpResponse setupHttpClientMockForSuccessfulResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody(SessionResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"iteraciones\":\"1\",\"semilla\":\"00000\",\"operacion\":\"+\",\"constante\":\"00000\"}",
                                SessionResponse.class));
        return httpResponse;
    }
}
