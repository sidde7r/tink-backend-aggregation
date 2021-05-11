package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class SdcApiClientTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sdc/resources";
    private static final String INTERNAL_ERROR_RESPONSE =
            readJsonFromFile("internal_error_response.json");
    private static final String OTHER_ERROR_RESPONSE =
            readJsonFromFile("other_error_response.json");
    private static final String TOKEN_EXPIRED_ERROR_RESPONSE =
            readJsonFromFile("token_expired_error_response.json");

    private static final String TOKEN_URL = "/Token";
    private static final String ACCOUNTS_URL = "/v1/accounts";
    private static final String BALANCES_URL = "/v1/accounts/.*/balances.*";
    private static final String TRANSACTIONS_URL = "/v1/accounts/.*/transactions.*";

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private PersistentStorage persistentStorage;

    private SdcApiClient apiClient;

    @Before
    public void setup() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        persistentStorage = mock(PersistentStorage.class);

        apiClient =
                new SdcApiClient(
                        httpClient,
                        new SdcUrlProvider(wireMock.baseUrl(), wireMock.baseUrl()),
                        persistentStorage,
                        new SdcConfiguration(),
                        null);
    }

    @Test
    public void should_throw_session_expired_when_there_is_no_access_token() {
        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken());

        // then
        assertThat(throwable)
                .isEqualToComparingFieldByField(SessionError.SESSION_EXPIRED.exception());

        verifyPostRequestedFor(TOKEN_URL, 0);
        verifyDoesNotUpdateAccessToken();
    }

    @Test
    public void should_throw_session_expired_when_there_is_no_refresh_token() {
        // given
        mockRefreshToken(null);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken());

        // then
        assertThat(throwable)
                .isEqualToComparingFieldByField(SessionError.SESSION_EXPIRED.exception());

        verifyPostRequestedFor(TOKEN_URL, 0);
        verifyDoesNotUpdateAccessToken();
    }

    @Test
    public void should_update_access_token_in_storage() {
        // given
        mockRefreshToken("SAMPLE_REFRESH_TOKEN");
        stubApiPostResponse(TOKEN_URL, 200);

        // when
        apiClient.refreshAccessToken();

        // then
        verifyPostRequestedFor(TOKEN_URL, 1);
        verifyUpdatesAccessToken();
    }

    @Test
    public void should_retry_three_times_and_then_fail_if_banks_internal_error_encountered() {
        // given
        mockRefreshToken("SAMPLE_REFRESH_TOKEN2");
        stubApiPostResponse(TOKEN_URL, 400, INTERNAL_ERROR_RESPONSE);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken());

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");

        verifyPostRequestedFor(TOKEN_URL, 3);
        verifyDoesNotUpdateAccessToken();
    }

    @Test
    public void should_not_retry_and_end_up_as_generic_exception_in_other_error_cases() {
        // given
        mockRefreshToken("SAMPLE_REFRESH_TOKEN3");
        stubApiPostResponse(TOKEN_URL, 400, OTHER_ERROR_RESPONSE);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken());

        // then
        assertThat(throwable).isInstanceOf(HttpResponseException.class);

        verifyPostRequestedFor(TOKEN_URL, 1);
        verifyDoesNotUpdateAccessToken();
    }

    @Test
    @Parameters(method = "urlWithApiClientMethodTestParams")
    public void should_refresh_token_and_repeat_fetching_when_token_expired(
            String apiUrl, Consumer<SdcApiClient> apiClientMethod) {
        // given
        mockRefreshToken("SAMPLE_REFRESH_TOKEN");
        stubApiPostResponse(TOKEN_URL, 200);
        stubApiTokenExpiredResponseAndThen200(apiUrl);

        // when
        apiClientMethod.accept(apiClient);

        // then
        verifyGetRequestedFor(apiUrl, 2);
        verifyPostRequestedFor(TOKEN_URL, 1);
        verifyUpdatesAccessToken();
    }

    @Test
    @Parameters(method = "urlWithApiClientMethodTestParams")
    public void should_try_to_handle_token_expired_error_only_once(
            String apiUrl, Consumer<SdcApiClient> apiClientMethod) {
        // given
        mockRefreshToken("SAMPLE_REFRESH_TOKEN");
        stubApiPostResponse(TOKEN_URL, 200);
        stubApiTokenExpiredResponse(apiUrl);

        // when
        Throwable throwable = catchThrowable(() -> apiClientMethod.accept(apiClient));

        // then
        assertThat(throwable).isInstanceOf(HttpResponseException.class);

        verifyGetRequestedFor(apiUrl, 2);
        verifyPostRequestedFor(TOKEN_URL, 1);
        verifyUpdatesAccessToken();
    }

    @SuppressWarnings("unused")
    private Object[] urlWithApiClientMethodTestParams() {
        return new Object[] {
            toArray(ACCOUNTS_URL, (Consumer<SdcApiClient>) SdcApiClient::fetchAccounts),
            toArray(
                    BALANCES_URL,
                    (Consumer<SdcApiClient>)
                            client -> client.fetchAccountBalances("SAMPLE_ACCOUNT_ID_123")),
            toArray(
                    TRANSACTIONS_URL,
                    (Consumer<SdcApiClient>)
                            client ->
                                    client.getTransactionsFor(
                                            "SAMPLE_ACCOUNT_ID_321",
                                            mock(Date.class),
                                            mock(Date.class),
                                            "SAMPLE_PROVIDER_MARKET",
                                            "both"))
        };
    }

    private void stubApiTokenExpiredResponseAndThen200(String urlPattern) {
        WireMock.stubFor(
                WireMock.get(urlMatching(urlPattern))
                        .inScenario("api401")
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(400)
                                        .withBody(TOKEN_EXPIRED_ERROR_RESPONSE)
                                        .withHeader("Content-Type", "application/json"))
                        .willSetStateTo("token_expired_error_thrown"));
        WireMock.stubFor(
                WireMock.get(urlMatching(urlPattern))
                        .inScenario("api401")
                        .whenScenarioStateIs("token_expired_error_thrown")
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody("{}")
                                        .withHeader("Content-Type", "application/json")));
    }

    private void stubApiTokenExpiredResponse(String urlPattern) {
        WireMock.stubFor(
                WireMock.get(urlMatching(urlPattern))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(400)
                                        .withBody(TOKEN_EXPIRED_ERROR_RESPONSE)
                                        .withHeader("Content-Type", "application/json")));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyPostRequestedFor(String urlPattern, int times) {
        WireMock.verify(times, postRequestedFor(urlMatching(urlPattern)));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGetRequestedFor(String urlPattern, int times) {
        WireMock.verify(times, getRequestedFor(urlMatching(urlPattern)));
    }

    private void verifyUpdatesAccessToken() {
        verify(persistentStorage).put(eq(StorageKeys.OAUTH_TOKEN), any(OAuth2Token.class));
    }

    private void verifyDoesNotUpdateAccessToken() {
        verify(persistentStorage, never()).put(eq(StorageKeys.OAUTH_TOKEN), any(OAuth2Token.class));
    }

    private void mockRefreshToken(@Nullable String refreshToken) {
        OAuth2Token sampleToken = mock(OAuth2Token.class);
        when(sampleToken.getRefreshToken()).thenReturn(Optional.ofNullable(refreshToken));

        when(persistentStorage.get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(sampleToken));
    }

    @SuppressWarnings("SameParameterValue")
    private void stubApiPostResponse(String urlPattern, int status) {
        stubApiPostResponse(urlPattern, status, "{}");
    }

    private void stubApiPostResponse(String urlPattern, int status, String body) {
        WireMock.stubFor(
                WireMock.post(urlPathMatching(urlPattern))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(status)
                                        .withBody(body)
                                        .withHeader("Content-Type", "application/json")));
    }

    private static Object[] toArray(Object... args) {
        return args;
    }

    @SneakyThrows
    private static String readJsonFromFile(String fileName) {
        File file = Paths.get(TEST_DATA_PATH, fileName).toFile();
        return new ObjectMapper().readTree(file).toString();
    }
}
