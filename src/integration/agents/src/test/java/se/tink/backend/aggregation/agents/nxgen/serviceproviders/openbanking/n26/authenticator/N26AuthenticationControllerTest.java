package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.TokenPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage.N26Storage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class N26AuthenticationControllerTest {

    private static final String SUPPLEMENTAL_KEY = "supplementalKey";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String CAUSE_SESSION_EXPIRED = "Cause: SessionError.SESSION_EXPIRED";
    private static final String QWAC =
            "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";

    private N26AuthenticationController authenticationController;
    private N26ApiClient apiClient;
    private AgentConfiguration<N26Configuration> configuration;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;
    private N26Storage storage;

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26/resources/";

    @Before
    public void setup() {
        apiClient = mock(N26ApiClient.class);
        configuration = mock(AgentConfiguration.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        storage = mock(N26Storage.class);
        authenticationController =
                new N26AuthenticationController(
                        apiClient,
                        configuration,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage,
                        new Credentials());
    }

    @Test
    public void shouldThrowExceptionWhenAccessTokenIsNullDuringAutoAuthentication() {
        // given
        when(storage.getAccessToken()).thenReturn(null);

        // then
        Assertions.assertThatExceptionOfType(SessionException.class)
                .isThrownBy(authenticationController::autoAuthenticate)
                .withNoCause()
                .withMessage(CAUSE_SESSION_EXPIRED);
    }

    @Test
    @SneakyThrows
    public void shouldCallForTokenInfoIfExpiryDateNotInStorage() {
        // given
        long currentTimeMillis = Instant.now().toEpochMilli() + 100000000L;
        when(storage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(storage.getAccessTokenExpiryDate())
                .thenReturn(Optional.empty(), Optional.of(currentTimeMillis));
        when(apiClient.tokenDetails(ACCESS_TOKEN))
                .thenReturn(createTokenDetailsResponse(currentTimeMillis));

        // when
        authenticationController.autoAuthenticate();

        // then
        verify(apiClient).tokenDetails(ACCESS_TOKEN);
        verify(storage).storeAccessTokenExpiryDate(currentTimeMillis);
    }

    @Test
    @SneakyThrows
    public void shouldThrowExceptionWhenNoExpiryDateInTokenDetailsResponse() {
        // given
        when(storage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(storage.getAccessTokenExpiryDate()).thenReturn(Optional.empty());
        when(apiClient.tokenDetails(ACCESS_TOKEN)).thenReturn(new TokenDetailsResponse());

        // when
        Assertions.assertThatExceptionOfType(SessionException.class)
                .isThrownBy(authenticationController::autoAuthenticate)
                .withNoCause()
                .withMessage(CAUSE_SESSION_EXPIRED);

        // then
        verify(storage, never()).storeAccessTokenExpiryDate(anyLong());
    }

    @Test
    @SneakyThrows
    public void shouldThrowExceptionWhenStoredExpiryDateExpired() {
        // given
        long currentTimeMillis = Instant.now().toEpochMilli() - 1000L;
        when(storage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(storage.getAccessTokenExpiryDate())
                .thenReturn(Optional.empty(), Optional.of(currentTimeMillis));
        when(apiClient.tokenDetails(ACCESS_TOKEN))
                .thenReturn(createTokenDetailsResponse(currentTimeMillis));

        // when
        Assertions.assertThatExceptionOfType(SessionException.class)
                .isThrownBy(authenticationController::autoAuthenticate)
                .withNoCause()
                .withMessage(CAUSE_SESSION_EXPIRED);

        // then
        verify(storage).clear();
    }

    @Test
    @SneakyThrows
    public void shouldPassAutoAuthentication() {
        // given
        long currentTimeMillis = Instant.now().toEpochMilli() + 100000000L;
        when(storage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(storage.getAccessTokenExpiryDate()).thenReturn(Optional.of(currentTimeMillis));

        // when
        authenticationController.autoAuthenticate();

        // then
        verify(storage, never()).clear();
        verify(apiClient, never()).tokenDetails(anyString());
    }

    @Test
    @SneakyThrows
    public void shouldStoreAccessTokenFromCallbackData() {
        shouldStoreAccessToken("tokenId");
    }

    @Test
    @SneakyThrows
    public void shouldStoreAccessTokenFromCallbackDataWhenTokenIdInLowerCase() {
        shouldStoreAccessToken("tokenid");
    }

    @Test
    public void shouldReturnThirdPartyAuthErrorWhenCallbackDataIsMissing() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(SUPPLEMENTAL_KEY), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(Collections.emptyMap()));

        // when
        ThirdPartyAppResponse<String> response = authenticationController.collect(null);

        // then
        assertThat(response)
                .isEqualToComparingFieldByField(
                        ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR));
    }

    @Test
    public void shouldReturnThirdPartyTimedOutWhenNoCallbackData() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(SUPPLEMENTAL_KEY), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.empty());

        // when
        ThirdPartyAppResponse<String> response = authenticationController.collect(null);

        // then
        assertThat(response)
                .isEqualToComparingFieldByField(
                        ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT));
    }

    @Test
    @SneakyThrows
    public void shouldCreateAppAuthPayload() {
        // given
        String finalAuthUrl = "https://web-app.token.io/app/request-token/" + ACCESS_TOKEN;
        N26Configuration n26Configuration = mock(N26Configuration.class);
        when(configuration.getQwac()).thenReturn(QWAC);
        when(apiClient.tokenRequest(any(TokenRequest.class)))
                .thenReturn(createTokenResponse(ACCESS_TOKEN));
        when(configuration.getProviderSpecificConfiguration()).thenReturn(n26Configuration);

        // when
        ThirdPartyAppAuthenticationPayload appPayload = authenticationController.getAppPayload();

        // then
        assertThat(appPayload.getDesktop().getUrl()).isEqualTo(finalAuthUrl);
        assertThat(appPayload.getIos().getDeepLinkUrl()).isEqualTo(finalAuthUrl);
        assertThat(appPayload.getAndroid().getIntent()).isEqualTo(finalAuthUrl);
    }

    @Test
    public void shouldSetSessionExpiryAfterCollect() {
        // given
        Date date = new Date(1602869683503L);

        TinkHttpClient tinkHttpClient = mockHttpClient();
        Credentials credentials = new Credentials();

        createAuthenticationController(tinkHttpClient, credentials);

        // when
        authenticationController.collect("reference");

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    private void createAuthenticationController(
            TinkHttpClient tinkHttpClient, Credentials credentials) {
        N26Configuration n26Configuration =
                new N26Configuration("apiKey", "memberId", "realmId", "redirectUrl");
        AgentConfiguration<N26Configuration> configuration =
                new AgentConfiguration.Builder<N26Configuration>()
                        .setProviderSpecificConfiguration(n26Configuration)
                        .build();

        N26Storage storage = new N26Storage(new PersistentStorage());
        N26ApiClient apiClient = new N26ApiClient(tinkHttpClient, configuration, storage);
        String supplementalKey = "supplementalKey";

        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementalKey);
        Map<String, String> callbackData = Maps.newHashMap("tokenId", ACCESS_TOKEN);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementalKey), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(callbackData));

        authenticationController =
                new N26AuthenticationController(
                        apiClient,
                        configuration,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage,
                        credentials);
    }

    private TinkHttpClient mockHttpClient() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mockBaseRequestBuilderCalls(tinkHttpClient);
        when(requestBuilder.get(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(TEST_DATA_PATH + "token_details_response.json"),
                                TokenDetailsResponse.class));
        return tinkHttpClient;
    }

    private RequestBuilder mockBaseRequestBuilderCalls(TinkHttpClient tinkHttpClient) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        return requestBuilder;
    }

    @SneakyThrows
    private void shouldStoreAccessToken(String tokenIdKey) {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);
        Map<String, String> callbackData = Maps.newHashMap(tokenIdKey, ACCESS_TOKEN);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(SUPPLEMENTAL_KEY), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(callbackData));
        when(apiClient.tokenDetails(null))
                .thenReturn(createTokenDetailsResponse(System.currentTimeMillis()));

        // when
        ThirdPartyAppResponse<String> response = authenticationController.collect(null);

        // then
        assertThat(response)
                .isEqualToComparingFieldByField(
                        ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        verify(storage).storeAccessToken(ACCESS_TOKEN);
    }

    private TokenDetailsResponse createTokenDetailsResponse(Long expiryDateInMs) {
        TokenDetailsResponse tokenDetailsResponse = new TokenDetailsResponse();
        TokenEntity tokenEntity = new TokenEntity();
        TokenPayloadEntity tokenPayloadEntity = new TokenPayloadEntity();
        tokenPayloadEntity.setExpiresAtMs(expiryDateInMs.toString());
        tokenEntity.setPayload(tokenPayloadEntity);
        tokenDetailsResponse.setToken(tokenEntity);
        return tokenDetailsResponse;
    }

    private TokenResponse createTokenResponse(String tokenId) {
        TokenResponse tokenResponse = new TokenResponse();
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setId(tokenId);
        tokenResponse.setTokenRequest(tokenRequest);
        return tokenResponse;
    }
}
