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

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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

public class N26AuthenticationControllerTest {

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String CAUSE_SESSION_EXPIRED = "Cause: SessionError.SESSION_EXPIRED";

    private N26AuthenticationController authenticationController;
    private N26ApiClient apiClient;
    private AgentConfiguration<N26Configuration> configuration;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;
    private N26Storage storage;

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
                        storage);
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
    @SneakyThrows
    public void shouldThrowExceptionWhenCallbackDataIsMissing() {
        // given
        String supplementalKey = "supplementalKey";
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementalKey);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementalKey), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(Collections.emptyMap()));

        // then
        Assertions.assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> authenticationController.collect(null))
                .withNoCause()
                .withMessage("callbackData didn't contain tokenId");
    }

    @Test
    @SneakyThrows
    public void shouldCreateAppAuthPayload() {
        // given
        String finalAuthUrl = "https://web-app.token.io/app/request-token/" + ACCESS_TOKEN;
        N26Configuration n26Configuration = mock(N26Configuration.class);
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

    @SneakyThrows
    private void shouldStoreAccessToken(String tokenIdKey) {
        // given
        String supplementalKey = "supplementalKey";
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(supplementalKey);
        Map<String, String> callbackData = Maps.newHashMap(tokenIdKey, ACCESS_TOKEN);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(supplementalKey), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(callbackData));

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
