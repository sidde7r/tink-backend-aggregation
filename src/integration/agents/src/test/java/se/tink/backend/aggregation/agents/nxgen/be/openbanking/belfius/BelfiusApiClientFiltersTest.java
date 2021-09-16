package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HttpClient.MAX_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServerErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class BelfiusApiClientFiltersTest {

    private static final URL TEST_URL = new URL("https://belfius.be/test");

    @Spy private BelfiusResponseStatusHandler belfiusResponseStatusHandler;
    @Mock private Filter nextFilter;
    @Spy private final ServerErrorFilter serverErrorFilter = new ServerErrorFilter();
    private final TimeoutFilter timeoutFilter = new TimeoutFilter();

    @Spy
    private final ConnectionTimeoutRetryFilter connectionTimeoutRetryFilter =
            new ConnectionTimeoutRetryFilter(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS);

    @Spy
    private final TerminatedHandshakeRetryFilter terminatedHandshakeRetryFilter =
            new TerminatedHandshakeRetryFilter();

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setup() {
        belfiusResponseStatusHandler = new BelfiusResponseStatusHandler(persistentStorage);
        MockitoAnnotations.openMocks(this);
        client.setResponseStatusHandler(belfiusResponseStatusHandler);
        client.addFilter(serverErrorFilter);
        client.addFilter(timeoutFilter);
        client.addFilter(connectionTimeoutRetryFilter);
        client.addFilter(terminatedHandshakeRetryFilter);
        client.addFilter(nextFilter);
        terminatedHandshakeRetryFilter.setNext(nextFilter);
    }

    /**
     * This method is used to check if filters set for BelfiusApiClient's TinkHttpClient are the
     * same as TinkHttpClient used in the test. In case of any change this method should fail, which
     * will be a notification for a developer to adjust tests to a new behaviour.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldSetSameFiltersAsBelfiusApiClient() {
        // given
        NextGenTinkHttpClient belfiusHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        // when
        new BelfiusApiClient(
                belfiusHttpClient,
                mock(AgentConfiguration.class),
                new MockRandomValueGenerator(),
                persistentStorage);

        // and
        client.removeFilter(nextFilter);
        terminatedHandshakeRetryFilter.setNext(null);

        // then
        assertThat(client)
                .extracting("filters")
                .usingRecursiveComparison()
                .ignoringFields("mockitoInterceptor")
                .isEqualTo(belfiusHttpClient.getFilters());

        // and
        assertThat(client.getResponseStatusHandler())
                .usingRecursiveComparison()
                .ignoringFields("mockitoInterceptor")
                .isEqualTo(belfiusHttpClient.getResponseStatusHandler());
    }

    @Test
    @Parameters(method = "errorsParams")
    public void shouldFilterAndThrowProperException(
            String exceptionMessage, RuntimeException expectedException) {
        // given
        HttpClientException exception = new HttpClientException(exceptionMessage, null);

        // and
        when(terminatedHandshakeRetryFilter.getNext().handle(any())).thenThrow(exception);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable).usingRecursiveComparison().isEqualTo(expectedException);

        // and
        verify(terminatedHandshakeRetryFilter, times(4)).shouldRetry(exception);
        verify(belfiusResponseStatusHandler, never()).handleResponse(any(), any());
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
        when(terminatedHandshakeRetryFilter.getNext().handle(any())).thenReturn(response);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.NO_BANK_SERVICE.exception("Http status: " + status));

        // and
        verify(terminatedHandshakeRetryFilter, never()).shouldRetry(any());
        verify(belfiusResponseStatusHandler, never()).handleResponse(any(), any());
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
        when(terminatedHandshakeRetryFilter.getNext().handle(any())).thenReturn(response);

        // when
        Throwable throwable = catchThrowable(() -> client.request(TEST_URL).get(String.class));

        // then
        assertThat(throwable)
                .usingRecursiveComparison()
                .isEqualTo(
                        SessionError.SESSION_EXPIRED.exception(
                                "User\\System has deactivated the consent"));
        assertThat(persistentStorage.isEmpty()).isTrue();

        // and
        verify(belfiusResponseStatusHandler).handleResponse(any(), eq(response));
        verify(terminatedHandshakeRetryFilter, never()).shouldRetry(any());
    }
}
