package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls.TOKEN_PATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter.BelfiusClientConfigurator;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class BelfiusApiClientFiltersTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius/resources/";

    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 1;
    private static final int EXPECTED_NO_OF_RETRIES = 2;
    private static final URL TEST_URL = new URL("https://belfius.be/test");
    private static final Date SESSION_EXPIRY_DATE =
            new Date(new ConstantLocalDateTimeSource().getSystemCurrentTimeMillis());
    private static final String STORAGE_KEY = "TEST";
    private static final String STORAGE_VALUE = "TEST_VALUE";
    private static final String REFRESH_TOKEN_REQUEST_BODY =
            "grant_type=refresh_token&refresh_token=some_token";
    private static final String UNKNOWN_ERROR_BODY = "Unknown test error";
    private static final String TEST_REQUEST_BODY = "Some body";

    @Mock private Filter callFilter;
    @Mock private HttpResponse response;

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        new BelfiusClientConfigurator(new ConstantLocalDateTimeSource())
                .configure(
                        client,
                        persistentStorage,
                        TEST_MAX_RETRIES_NUMBER,
                        TEST_RETRY_SLEEP_MS,
                        SESSION_EXPIRY_DATE);
        client.addFilter(callFilter);
        persistentStorage.put(STORAGE_KEY, STORAGE_VALUE);
    }

    @Test
    @Parameters
    public void shouldRetryAndThrowProperException(
            String exceptionMessage, RuntimeException expectedException) {
        // given
        HttpClientException exception = new HttpClientException(exceptionMessage, null);

        // and
        given(callFilter.handle(any())).willThrow(exception);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .usingRecursiveComparison()
                .isEqualTo(expectedException);
        then(callFilter).should(times(EXPECTED_NO_OF_RETRIES)).handle(any());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldRetryAndThrowProperException() {
        return new Object[][] {
            {
                "Remote host terminated the handshake",
                new HttpClientException("Remote host terminated the handshake", null)
            },
            {
                "connect timed out",
                BankServiceError.BANK_SIDE_FAILURE.exception(
                        new HttpClientException("connect timed out", null))
            },
        };
    }

    @Test
    @Parameters
    public void shouldThrowOnBankServerError(int status, String requestBody, String responseBody) {
        // given
        given(response.getStatus()).willReturn(status);
        given(response.getBody(String.class)).willReturn(responseBody);

        // and
        given(callFilter.handle(any())).willReturn(response);

        // expect
        assertThatThrownBy(
                        () ->
                                client.request(URL.of(TOKEN_PATH))
                                        .body(requestBody)
                                        .get(String.class))
                .isInstanceOf(BankServiceException.class)
                .hasFieldOrPropertyWithValue("error", BankServiceError.NO_BANK_SERVICE);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldThrowOnBankServerError() {
        return new Object[][] {
            {500, null, null},
            {500, "", null},
            {500, TEST_REQUEST_BODY, null},
            {500, REFRESH_TOKEN_REQUEST_BODY, null},
            {500, REFRESH_TOKEN_REQUEST_BODY, ""},
            {500, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
            {502, null, null},
            {502, "", null},
            {502, TEST_REQUEST_BODY, null},
            {502, REFRESH_TOKEN_REQUEST_BODY, null},
            {502, REFRESH_TOKEN_REQUEST_BODY, ""},
            {502, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
            {503, null, null},
            {503, "", null},
            {503, TEST_REQUEST_BODY, null},
            {503, REFRESH_TOKEN_REQUEST_BODY, null},
            {503, REFRESH_TOKEN_REQUEST_BODY, ""},
            {503, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
            {504, null, null},
            {504, "", null},
            {504, TEST_REQUEST_BODY, null},
            {504, REFRESH_TOKEN_REQUEST_BODY, null},
            {504, REFRESH_TOKEN_REQUEST_BODY, ""},
            {504, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
        };
    }

    @Test
    @Parameters
    public <T> void shouldExpireSessionOnConsentAndTokenErrors(
            int status, Class<T> responseBodyClass, T responseBody) {
        // given
        given(response.getStatus()).willReturn(status);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(responseBodyClass)).willReturn(responseBody);

        // and
        given(callFilter.handle(any())).willReturn(response);

        // expect
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(SessionException.class)
                .hasFieldOrPropertyWithValue("error", SessionError.SESSION_EXPIRED);
        assertThat(persistentStorage.isEmpty()).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldExpireSessionOnConsentAndTokenErrors() {
        return new Object[][] {
            {403, String.class, readFileToString(RESOURCES_PATH + "no_active_consent.json")},
            {
                401,
                ErrorResponse.class,
                deserializeFromFile(
                        RESOURCES_PATH + "refresh_token_invalid.json", ErrorResponse.class)
            },
            {
                401,
                ErrorResponse.class,
                deserializeFromFile(
                        RESOURCES_PATH + "refresh_token_invalid_v3.json", ErrorResponse.class)
            },
            {
                401,
                ErrorResponse.class,
                deserializeFromFile(
                        RESOURCES_PATH + "access_token_invalid.json", ErrorResponse.class)
            },
            {
                401,
                ErrorResponse.class,
                deserializeFromFile(
                        RESOURCES_PATH + "access_token_invalid_v2.json", ErrorResponse.class)
            }
        };
    }

    @Test
    @Parameters({"400", "401", "402", "403", "404", "405", "406", "407", "409", "429"})
    public void shouldNotExpireSessionOnResponseWithoutBodyAndNotRefreshTokenEndpoint(int status) {
        // given
        given(response.getStatus()).willReturn(status);

        // and
        given(callFilter.handle(any())).willReturn(response);

        // expect
        assertHttpResponseExceptionAndPersistentStorageNotCleared();
    }

    @Test
    @Parameters
    public <T> void shouldNotExpireSessionOnKnownStatusWithBodyAndNotRefreshTokenEndpoint(
            int status, Class<T> responseBodyClass, T responseBody) {
        // given
        given(response.getStatus()).willReturn(status);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(responseBodyClass)).willReturn(responseBody);

        // and
        given(callFilter.handle(any())).willReturn(response);

        // expect
        assertHttpResponseExceptionAndPersistentStorageNotCleared();
    }

    @SuppressWarnings("unused")
    private Object[]
            parametersForShouldNotExpireSessionOnKnownStatusWithBodyAndNotRefreshTokenEndpoint() {
        return new Object[][] {
            {
                403, String.class, readFileToString(RESOURCES_PATH + "test_error.json"),
            },
            {403, String.class, UNKNOWN_ERROR_BODY},
            {403, String.class, ""},
            {
                401,
                ErrorResponse.class,
                deserializeFromFile(RESOURCES_PATH + "test_error.json", ErrorResponse.class)
            },
            {401, ErrorResponse.class, null},
            {401, ErrorResponse.class, new ErrorResponse()},
        };
    }

    @Test
    @Parameters
    public void shouldNotExpireSessionOnUnknownErrorAndRefreshTokenEndpoint(
            int status, String requestBody, String responseBody) {
        // given
        given(response.getStatus()).willReturn(status);
        given(response.getBody(String.class)).willReturn(responseBody);

        // and
        given(callFilter.handle(any())).willReturn(response);

        // expect
        assertThatThrownBy(
                        () ->
                                client.request(URL.of(TOKEN_PATH))
                                        .body(requestBody)
                                        .get(String.class))
                .isInstanceOf(HttpResponseException.class);
        assertThat(persistentStorage).containsEntry(STORAGE_KEY, STORAGE_VALUE);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldNotExpireSessionOnUnknownErrorAndRefreshTokenEndpoint() {
        return new Object[][] {
            {400, null, null},
            {400, "", null},
            {400, TEST_REQUEST_BODY, null},
            {400, REFRESH_TOKEN_REQUEST_BODY, null},
            {400, REFRESH_TOKEN_REQUEST_BODY, ""},
            {400, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
            {401, null, null},
            {401, "", null},
            {401, TEST_REQUEST_BODY, null},
            {401, REFRESH_TOKEN_REQUEST_BODY, null},
            {401, REFRESH_TOKEN_REQUEST_BODY, ""},
            {401, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY},
            {403, null, null},
            {403, "", null},
            {403, TEST_REQUEST_BODY, null},
            {403, REFRESH_TOKEN_REQUEST_BODY, null},
            {403, REFRESH_TOKEN_REQUEST_BODY, ""},
            {403, REFRESH_TOKEN_REQUEST_BODY, UNKNOWN_ERROR_BODY}
        };
    }

    private void assertHttpResponseExceptionAndPersistentStorageNotCleared() {
        assertThatThrownBy(() -> client.request(TEST_URL).get(String.class))
                .isInstanceOf(HttpResponseException.class);
        assertThat(persistentStorage).containsEntry(STORAGE_KEY, STORAGE_VALUE);
    }

    private static <T> T deserializeFromFile(String filePath, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(Paths.get(filePath).toFile(), clazz);
    }

    private static String readFileToString(String filePath) {
        try {
            return FileUtils.readFileToString(Paths.get(filePath).toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
