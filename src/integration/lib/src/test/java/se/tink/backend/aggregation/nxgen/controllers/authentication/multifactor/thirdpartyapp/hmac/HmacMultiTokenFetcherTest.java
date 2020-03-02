package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenRefreshStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.TokenLifeTime;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

public class HmacMultiTokenFetcherTest {

    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "4321";
    private static final long VALID_EXPIRES_IN_SECONDS = 100_000L;

    private HmacMultiTokenFetcher hmacMultiTokenFetcher;

    private HmacMultiTokenStorage hmacMultiTokenStorageMock;
    private AccessTokenProvider<HmacToken> accessTokenProviderMock;
    private AccessCodeStorage accessCodeStorageMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        final Credentials credentialsMock = mock(Credentials.class);

        accessTokenProviderMock = mock(AccessTokenProvider.class);
        hmacMultiTokenStorageMock = mock(HmacMultiTokenStorage.class);
        accessCodeStorageMock = mock(AccessCodeStorage.class);

        final AccessTokenFetchHelper<HmacToken> accessTokenFetchHelper =
                new AccessTokenFetchHelper<>(
                        accessTokenProviderMock,
                        credentialsMock,
                        new TokenLifeTime(
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME,
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME_UNIT));

        hmacMultiTokenFetcher =
                new HmacMultiTokenFetcher(
                        accessTokenFetchHelper, hmacMultiTokenStorageMock, accessCodeStorageMock);
    }

    @Test
    public void shouldGetAccessTokenStatusForValidToken() {
        // given
        when(hmacMultiTokenStorageMock.getToken())
                .thenReturn(Optional.of(createMultiToken(REFRESH_TOKEN, VALID_EXPIRES_IN_SECONDS)));

        // when
        final AccessTokenStatus returnedStatus = hmacMultiTokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.VALID);
        verify(hmacMultiTokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldGetAccessTokenStatusForExpiredAccessToken() {
        // given
        final HmacMultiToken hmacMultiToken = createMultiToken(REFRESH_TOKEN, 0L);
        when(hmacMultiTokenStorageMock.getToken()).thenReturn(Optional.of(hmacMultiToken));

        // when
        final AccessTokenStatus returnedStatus = hmacMultiTokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.EXPIRED);
        verify(hmacMultiTokenStorageMock).storeTokenInSession(hmacMultiToken);
    }

    @Test
    public void shouldGetAccessTokenStatusForExpiredTokenAndAbsentRefreshToken() {
        // given
        when(hmacMultiTokenStorageMock.getToken())
                .thenReturn(Optional.of(createMultiToken(null, 0L)));

        // when
        final AccessTokenStatus returnedStatus = hmacMultiTokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.NOT_PRESENT);
        verify(hmacMultiTokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldGetAccessTokenStatusWhenMultiTokenIsNotPresent() {
        // given
        when(hmacMultiTokenStorageMock.getToken()).thenReturn(Optional.empty());

        // when
        final AccessTokenStatus returnedStatus = hmacMultiTokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.NOT_PRESENT);
        verify(hmacMultiTokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final HmacMultiToken hmacMultiToken = createMultiToken(REFRESH_TOKEN, 0L);
        when(hmacMultiTokenStorageMock.getTokenFromSession())
                .thenReturn(Optional.of(hmacMultiToken));

        final String newRefreshToken = "9988";
        final HmacToken refreshedToken = createHmacToken(newRefreshToken, VALID_EXPIRES_IN_SECONDS);
        when(accessTokenProviderMock.refreshAccessToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshedToken));

        // when
        final AccessTokenRefreshStatus returnedStatus = hmacMultiTokenFetcher.refreshAccessToken();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenRefreshStatus.SUCCESS);
        verify(hmacMultiTokenStorageMock)
                .rotateToken(createMultiToken(Collections.singletonList(refreshedToken)));
    }

    @Test
    public void shouldReturnFailedStatusIfCannotRefreshAccessToken() {
        // given
        final HmacMultiToken hmacMultiToken = createMultiToken(REFRESH_TOKEN, 0L);
        when(hmacMultiTokenStorageMock.getTokenFromSession())
                .thenReturn(Optional.of(hmacMultiToken));

        when(accessTokenProviderMock.refreshAccessToken(REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        // when
        final AccessTokenRefreshStatus returnedStatus = hmacMultiTokenFetcher.refreshAccessToken();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenRefreshStatus.FAILED);
        verify(hmacMultiTokenStorageMock, never()).rotateToken(any());
    }

    @Test
    public void shouldThrowExceptionIfCannotFindTokenToRefresh() {
        // given
        when(hmacMultiTokenStorageMock.getTokenFromSession()).thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(() -> hmacMultiTokenFetcher.refreshAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find token in session.");
        verify(hmacMultiTokenStorageMock, never()).rotateToken(any());
    }

    @Test
    public void shouldRetrieveAccessToken() {
        // given
        final String accessCode1 = "1357";
        final String accessCode2 = "0864";
        when(accessCodeStorageMock.getAccessCodeFromSession())
                .thenReturn(Optional.of(accessCode1 + "," + accessCode2));

        final HmacToken hmacToken1 = createHmacToken(REFRESH_TOKEN + "X", VALID_EXPIRES_IN_SECONDS);
        final HmacToken hmacToken2 = createHmacToken(REFRESH_TOKEN + "Y", VALID_EXPIRES_IN_SECONDS);

        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode1)).thenReturn(hmacToken1);
        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode2)).thenReturn(hmacToken2);

        // when
        hmacMultiTokenFetcher.retrieveAccessToken();

        // then
        final HmacMultiToken expectedHmacMultiToken =
                createMultiToken(Arrays.asList(hmacToken1, hmacToken2));
        verify(hmacMultiTokenStorageMock).storeToken(expectedHmacMultiToken);
    }

    @Test
    public void shouldThrowExceptionIfRetrievedInvalidAccessToken() {
        // given
        final String accessCode1 = "1357";
        final String accessCode2 = "0864";
        when(accessCodeStorageMock.getAccessCodeFromSession())
                .thenReturn(Optional.of(accessCode1 + "," + accessCode2));

        final HmacToken hmacToken1 = createHmacToken(REFRESH_TOKEN + "X", 0L);
        final HmacToken hmacToken2 = createHmacToken(REFRESH_TOKEN + "Y", VALID_EXPIRES_IN_SECONDS);

        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode1)).thenReturn(hmacToken1);
        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode2)).thenReturn(hmacToken2);

        // when
        final Throwable thrown = catchThrowable(() -> hmacMultiTokenFetcher.retrieveAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid access token.");
        verify(hmacMultiTokenStorageMock, never()).storeToken(any());
    }

    @Test
    public void shouldThrowExceptionIfThereAreNotAccessCodeStoredInSession() {
        // given
        when(accessCodeStorageMock.getAccessCodeFromSession()).thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(() -> hmacMultiTokenFetcher.retrieveAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find access code in session.");
        verify(hmacMultiTokenStorageMock, never()).storeToken(any());
    }

    private static HmacMultiToken createMultiToken(String refreshToken, long expiresInSeconds) {
        final List<HmacToken> tokens =
                Collections.singletonList(createHmacToken(refreshToken, expiresInSeconds));

        return createMultiToken(tokens);
    }

    private static HmacMultiToken createMultiToken(List<HmacToken> tokens) {
        return new HmacMultiToken(tokens);
    }

    private static HmacToken createHmacToken(String refreshToken, long expiresInSeconds) {
        return new HmacToken(
                HmacToken.MAC_TOKEN_TYPE,
                ACCESS_TOKEN,
                refreshToken,
                UUID.randomUUID().toString(),
                expiresInSeconds);
    }
}
