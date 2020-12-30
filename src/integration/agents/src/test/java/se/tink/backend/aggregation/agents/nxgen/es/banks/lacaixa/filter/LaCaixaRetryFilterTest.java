package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.RetryFilterValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LaCaixaRetryFilterTest {

    private LaCaixaRetryFilter laCaixaRetryFilter;

    private static Object temporaryProblemErrorCodes() {
        return ErrorCode.TEMPORARY_PROBLEM;
    }

    @Before
    public void setup() {
        laCaixaRetryFilter =
                new LaCaixaRetryFilter(
                        RetryFilterValues.MAX_ATTEMPTS, RetryFilterValues.RETRY_SLEEP_MILLISECONDS);
    }

    @Test
    @Parameters(method = "temporaryProblemErrorCodes")
    public void shouldRetry(String errorCode) {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = setupHttpClientMockFor409StatusByErrorCode(errorCode);
        when(nextFilter.handle(any())).thenReturn(response);

        laCaixaRetryFilter.setNext(nextFilter);
        laCaixaRetryFilter.handle(null);

        verify(nextFilter, times(RetryFilterValues.MAX_ATTEMPTS + 1)).handle(any());
    }

    @Test
    public void shouldNotRetryForDifferentErrorCode() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = setupHttpClientMockFor409StatusByErrorCode("0000");
        when(nextFilter.handle(any())).thenReturn(response);

        laCaixaRetryFilter.setNext(nextFilter);
        laCaixaRetryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    @Test
    public void shouldNotRetryForSuccessfulResponse() {
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = setupHttpClientMockForSuccessfulResponse();
        when(nextFilter.handle(any())).thenReturn(response);

        laCaixaRetryFilter.setNext(nextFilter);
        laCaixaRetryFilter.handle(null);

        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse setupHttpClientMockFor409StatusByErrorCode(String errorCode) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(409);
        when(httpResponse.getBody(LaCaixaErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"codigo\":\"" + errorCode + "\",\"mensaje\":\"ERROR\"}",
                                LaCaixaErrorResponse.class));
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
