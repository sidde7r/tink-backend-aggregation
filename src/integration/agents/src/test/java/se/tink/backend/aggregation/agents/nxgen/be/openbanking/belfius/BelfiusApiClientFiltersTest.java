package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class BelfiusApiClientFiltersTest {

    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 1;
    private static final URL TEST_URL = new URL("https://belfius.be/test");

    @Mock private Filter nextFilter;

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        new BelfiusClientConfigurator()
                .configure(client, persistentStorage, TEST_RETRY_SLEEP_MS, TEST_MAX_RETRIES_NUMBER);
        client.addFilter(nextFilter);
    }

    @Test
    @Parameters(method = "errorsParams")
    public void shouldFilterAndThrowProperException(
            String exceptionMessage, RuntimeException expectedException) {
        // given
        HttpClientException exception = new HttpClientException(exceptionMessage, null);

        // and
        when(nextFilter.handle(any())).thenThrow(exception);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable).usingRecursiveComparison().isEqualTo(expectedException);
    }

    private Object[] errorsParams() {
        return new Object[] {
            new Object[] {
                "Remote host terminated the handshake",
                new HttpClientException("Remote host terminated the handshake", null)
            },
            new Object[] {
                "connect timed out",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("connect timed out", null))
            },
        };
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowBankServiceExceptionOnServerError(int status) {
        // given
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(status);

        // and
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.NO_BANK_SERVICE.exception("Http status: " + status));
    }

    @Test
    public void shouldExpireSessionOnNoActiveConsentError() {
        // given
        persistentStorage.put("TEST", "TEST_VALUE");

        // and
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(403);
        when(response.getBody(String.class))
                .thenReturn(
                        "{\"error_description\":\"User\\/System has deactivated the consent. Tpp has to start over with the api \\/consent-uris\",\"error_code\":\"20005\",\"error\":\"no_active_consent\"}");

        // and
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(
                        SessionError.SESSION_EXPIRED.exception(
                                "User\\System has deactivated the consent"));
        assertThat(persistentStorage.isEmpty()).isTrue();
    }
}
