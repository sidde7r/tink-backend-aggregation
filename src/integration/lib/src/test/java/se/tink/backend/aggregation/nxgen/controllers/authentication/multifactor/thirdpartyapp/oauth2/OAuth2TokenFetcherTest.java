package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenRefreshStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.TokenLifeTime;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class OAuth2TokenFetcherTest {

    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "4321";
    private static final long VALID_EXPIRES_IN_SECONDS = 100_000L;

    private OAuth2TokenFetcher oAuth2TokenFetcher;

    private OAuth2TokenStorage oAuth2TokenStorageMock;
    private AccessTokenProvider<OAuth2Token> accessTokenProviderMock;
    private AccessCodeStorage accessCodeStorageMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        final Credentials credentialsMock = mock(Credentials.class);

        accessTokenProviderMock = mock(AccessTokenProvider.class);
        oAuth2TokenStorageMock = mock(OAuth2TokenStorage.class);
        accessCodeStorageMock = mock(AccessCodeStorage.class);

        final AccessTokenFetchHelper<OAuth2Token> accessTokenFetchHelper =
                new AccessTokenFetchHelper<>(
                        accessTokenProviderMock,
                        credentialsMock,
                        new TokenLifeTime(
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME,
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME_UNIT));

        oAuth2TokenFetcher =
                new OAuth2TokenFetcher(
                        accessTokenFetchHelper, oAuth2TokenStorageMock, accessCodeStorageMock);
    }

    @Test
    public void shouldGetAccessTokenStatusForValidToken() {
        // given
        when(oAuth2TokenStorageMock.getToken())
                .thenReturn(
                        Optional.of(createOAuth2Token(REFRESH_TOKEN, VALID_EXPIRES_IN_SECONDS)));

        // when
        final AccessTokenStatus returnedStatus = oAuth2TokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.VALID);
        verify(oAuth2TokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldGetAccessTokenStatusForExpiredAccessToken() {
        // given
        final OAuth2Token oAuth2Token = createOAuth2Token(REFRESH_TOKEN, 0L);
        when(oAuth2TokenStorageMock.getToken()).thenReturn(Optional.of(oAuth2Token));

        // when
        final AccessTokenStatus returnedStatus = oAuth2TokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.EXPIRED);
        verify(oAuth2TokenStorageMock).storeTokenInSession(oAuth2Token);
    }

    @Test
    public void shouldGetAccessTokenStatusForExpiredTokenAndAbsentRefreshToken() {
        // given
        when(oAuth2TokenStorageMock.getToken())
                .thenReturn(Optional.of(createOAuth2Token(null, 0L)));

        // when
        final AccessTokenStatus returnedStatus = oAuth2TokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.NOT_PRESENT);
        verify(oAuth2TokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldGetAccessTokenStatusWhenMultiTokenIsNotPresent() {
        // given
        when(oAuth2TokenStorageMock.getToken()).thenReturn(Optional.empty());

        // when
        final AccessTokenStatus returnedStatus = oAuth2TokenFetcher.getAccessTokenStatus();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.NOT_PRESENT);
        verify(oAuth2TokenStorageMock, never()).storeTokenInSession(any());
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final OAuth2Token oAuth2Token = createOAuth2Token(REFRESH_TOKEN, 0L);
        when(oAuth2TokenStorageMock.getTokenFromSession()).thenReturn(Optional.of(oAuth2Token));

        final String newRefreshToken = "9988";
        final OAuth2Token refreshedToken =
                createOAuth2Token(newRefreshToken, VALID_EXPIRES_IN_SECONDS);
        when(accessTokenProviderMock.refreshAccessToken(REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshedToken));

        // when
        final AccessTokenRefreshStatus returnedStatus = oAuth2TokenFetcher.refreshAccessToken();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenRefreshStatus.SUCCESS);
        verify(oAuth2TokenStorageMock).rotateToken(refreshedToken);
    }

    @Test
    public void shouldReturnFailedStatusIfCannotRefreshAccessToken() {
        // given
        final OAuth2Token oAuth2Token = createOAuth2Token(REFRESH_TOKEN, 0L);
        when(oAuth2TokenStorageMock.getTokenFromSession()).thenReturn(Optional.of(oAuth2Token));

        when(accessTokenProviderMock.refreshAccessToken(REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        // when
        final AccessTokenRefreshStatus returnedStatus = oAuth2TokenFetcher.refreshAccessToken();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenRefreshStatus.FAILED);
        verify(oAuth2TokenStorageMock, never()).rotateToken(any());
    }

    @Test
    public void shouldThrowExceptionIfCannotFindTokenToRefresh() {
        // given
        when(oAuth2TokenStorageMock.getTokenFromSession()).thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(() -> oAuth2TokenFetcher.refreshAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find token in session.");
        verify(oAuth2TokenStorageMock, never()).rotateToken(any());
    }

    @Test
    public void shouldRetrieveAccessToken() {
        // given
        final String accessCode = "1357";
        when(accessCodeStorageMock.getAccessCodeFromSession()).thenReturn(Optional.of(accessCode));

        final OAuth2Token oAuth2Token =
                createOAuth2Token(REFRESH_TOKEN + "X", VALID_EXPIRES_IN_SECONDS);

        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode)).thenReturn(oAuth2Token);

        // when
        oAuth2TokenFetcher.retrieveAccessToken();

        // then
        verify(oAuth2TokenStorageMock).storeToken(oAuth2Token);
    }

    @Test
    public void shouldThrowExceptionIfRetrievedInvalidAccessToken() {
        // given
        final String accessCode = "1357";
        when(accessCodeStorageMock.getAccessCodeFromSession()).thenReturn(Optional.of(accessCode));

        final OAuth2Token oAuth2Token = createOAuth2Token(REFRESH_TOKEN + "X", 0L);

        when(accessTokenProviderMock.exchangeAuthorizationCode(accessCode)).thenReturn(oAuth2Token);

        // when
        final Throwable thrown = catchThrowable(() -> oAuth2TokenFetcher.retrieveAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid access token.");
        verify(oAuth2TokenStorageMock, never()).storeToken(any());
    }

    @Test
    public void shouldThrowExceptionIfThereAreNotAccessCodeStoredInSession() {
        // given
        when(accessCodeStorageMock.getAccessCodeFromSession()).thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(() -> oAuth2TokenFetcher.retrieveAccessToken());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot find access code in session.");
        verify(oAuth2TokenStorageMock, never()).storeToken(any());
    }

    private static OAuth2Token createOAuth2Token(String refreshToken, long expiresInSeconds) {
        return OAuth2Token.create("bearer", ACCESS_TOKEN, refreshToken, expiresInSeconds);
    }
}
