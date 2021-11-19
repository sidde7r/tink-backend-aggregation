package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.filter.SodexoClientConfigurator;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(JUnitParamsRunner.class)
public class SodexoApiClientFiltersTest {

    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 2;
    private static final int NO_OF_FILTER_RETRIES = 3;
    private static final URL TEST_URL = new URL("https://sodexo.pt/test");
    private static final String CONNECTION_RESET_MESSAGE = "connection reset";
    private static final String CONNECT_TIMED_OUT_MESSAGE = "connect timed out";
    private static final String READ_TIMED_OUT_MESSAGE = "read timed out";
    private static final String FAILED_TO_RESPOND_MESSAGE = "failed to respond";

    @Mock private Filter callFilter;
    @Mock private HttpResponse response;

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        new SodexoClientConfigurator()
                .applyFilters(client, TEST_MAX_RETRIES_NUMBER, TEST_RETRY_SLEEP_MS);
        client.addFilter(callFilter);
        given(callFilter.handle(any())).willReturn(response);
    }

    @Test
    @Parameters
    public void shouldRetryAndThrowOnTimeoutErrors(
            String exceptionMessage, RuntimeException expectedException) {
        // given
        given(callFilter.handle(any())).willThrow(new HttpClientException(exceptionMessage, null));

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(BankServiceException.class)
                .hasMessage(expectedException.getMessage())
                .hasCause(expectedException.getCause());

        // and
        then(callFilter).should(times(NO_OF_FILTER_RETRIES)).handle(any());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldRetryAndThrowOnTimeoutErrors() {
        return new Object[][] {
            {
                CONNECTION_RESET_MESSAGE,
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException(CONNECTION_RESET_MESSAGE, null))
            },
            {
                CONNECT_TIMED_OUT_MESSAGE,
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException(CONNECT_TIMED_OUT_MESSAGE, null))
            },
            {
                READ_TIMED_OUT_MESSAGE,
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException(READ_TIMED_OUT_MESSAGE, null))
            },
            {
                FAILED_TO_RESPOND_MESSAGE,
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException(FAILED_TO_RESPOND_MESSAGE, null))
            },
        };
    }

    @Test
    @Parameters({"400", "401", "403", "404", "405", "409", "429", "501", "502", "503", "504"})
    public void shouldThrowOnOtherErrorsWithoutRetrying(int status) {
        // given
        given(response.getStatus()).willReturn(status);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(HttpResponseException.class)
                .hasMessage(String.format("Response statusCode: %s with body: null", status));

        // and
        then(callFilter).should().handle(any());
    }
}
