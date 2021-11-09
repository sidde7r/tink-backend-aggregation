package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSApiClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRateLimitFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRetryFilterProperties;
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
public class ICSApiClientFiltersTest {

    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 2;
    private static final int NO_OF_FILTER_RETRIES = 3;
    private static final URL TEST_URL = new URL("https://ics.nl/test");

    @Mock private Filter nextFilter;
    @Mock private HttpResponse response;

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        new ICSApiClientConfigurator()
                .applyFilters(
                        client,
                        new ICSRetryFilterProperties(TEST_MAX_RETRIES_NUMBER, TEST_RETRY_SLEEP_MS),
                        new ICSRateLimitFilterProperties(
                                TEST_RETRY_SLEEP_MS, TEST_RETRY_SLEEP_MS, TEST_MAX_RETRIES_NUMBER),
                        "nl-ics-oauth2");
        client.addFilter(nextFilter);
        when(nextFilter.handle(any())).thenReturn(response);
    }

    @Test
    @Parameters
    public void shouldFilterAndThrowProperException(
            String exceptionMessage, RuntimeException expectedException) {
        // given
        HttpClientException exception = new HttpClientException(exceptionMessage, null);

        // and
        given(nextFilter.handle(any())).willThrow(exception);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(expectedException.getClass())
                .hasMessage(expectedException.getMessage())
                .hasCause(expectedException.getCause());
    }

    private Object[] parametersForShouldFilterAndThrowProperException() {
        return new Object[] {
            new Object[] {
                "Remote host terminated the handshake",
                new HttpClientException("Remote host terminated the handshake", null)
            },
            new Object[] {
                "connection reset",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("connection reset", null))
            },
            new Object[] {
                "connect timed out",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("connect timed out", null))
            },
            new Object[] {
                "read timed out",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("read timed out", null))
            },
            new Object[] {
                "failed to respond",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("failed to respond", null))
            },
        };
    }

    @Test
    public void shouldRetryAndThrowOnInternalServerError() {
        // given
        given(response.getStatus()).willReturn(500);
        given(nextFilter.handle(any())).willReturn(response);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: 500")
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);

        // and
        verify(nextFilter, times(NO_OF_FILTER_RETRIES)).handle(any());
    }

    @Test
    @Parameters({"400", "401", "403", "501", "502", "503", "555"})
    public void shouldThrowOnOtherErrorsWithoutRetrying(int status) {
        // given
        given(response.getStatus()).willReturn(status);
        given(nextFilter.handle(any())).willReturn(response);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(HttpResponseException.class)
                .hasMessage(String.format("Response statusCode: %s with body: null", status));

        // and
        verify(nextFilter).handle(any());
    }

    @Test
    @Parameters
    public void shouldThrowOn429ErrorAndRetryOnTooManyRequests(
            String message, String errorMessage, int wantedNumberOfInvocations) {
        // given
        given(response.getStatus()).willReturn(429);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(String.class)).willReturn(message);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: 429 Error body: " + errorMessage)
                .hasFieldOrPropertyWithValue("error", BankServiceError.ACCESS_EXCEEDED);

        // and
        verify(nextFilter, times(wantedNumberOfInvocations)).handle(any());
    }

    private Object[] parametersForShouldThrowOn429ErrorAndRetryOnTooManyRequests() {
        return new Object[] {
            new Object[] {"Too many requests", "Too many requests", NO_OF_FILTER_RETRIES},
            new Object[] {"Access exceeded on account", "access exceeded on account", 1},
        };
    }
}
