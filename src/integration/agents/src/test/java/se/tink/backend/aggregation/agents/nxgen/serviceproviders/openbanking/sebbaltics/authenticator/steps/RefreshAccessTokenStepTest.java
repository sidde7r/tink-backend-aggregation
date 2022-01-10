package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RefreshAccessTokenStepTest {
    private SebBalticsApiClient apiClient;
    private RefreshAccessTokenStep refreshAccessTokenStep;

    @Before
    public void setUp() {
        apiClient = mock(SebBalticsApiClient.class);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        Credentials credentials = mock(Credentials.class);
        refreshAccessTokenStep =
                new RefreshAccessTokenStep(apiClient, persistentStorage, credentials);
    }

    @Test
    public void shouldReturnFalseWhenRefreshTokenExpiryAbsent() {
        Assert.assertFalse(
                refreshAccessTokenStep
                        .refreshToken(getOAuth2TokenWithoutRefreshTokenExpiry())
                        .isAuthenticationFinished());
    }

    @Test
    public void shouldReturnFalseWhenRefreshTokenAbsent() {
        Assert.assertFalse(
                refreshAccessTokenStep
                        .refreshToken(getOAuth2TokenWithoutRefreshToken())
                        .isAuthenticationFinished());
    }

    @Test
    public void shouldReturnFalseWhenRefreshedTokenResponseIsNull() {
        when(apiClient.getDecoupledToken(Mockito.any()))
                .thenReturn(getEmptyTokenResponseAfterRefresh());
        Assert.assertFalse(
                refreshAccessTokenStep
                        .refreshToken(getValidOAuth2Token())
                        .isAuthenticationFinished());
    }

    @Test
    public void shouldThrowSessionExceptionWhenErrorResponseFromBank() {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        // when
        when(response.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(response.getBody(ErrorResponse.class)).thenReturn(getInvalidTokenErrorResponse());
        when(apiClient.getDecoupledToken(Mockito.any()))
                .thenThrow(new HttpResponseException(request, response));

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        refreshAccessTokenStep,
                                        "refreshToken",
                                        getValidOAuth2Token()))
                .isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldReturnFalseWhenRefreshedTokenMissingAccessToken() {
        when(apiClient.getDecoupledToken(Mockito.any()))
                .thenReturn(getTokenResponseWithAccessTokenMissingAfterRefresh());
        Assert.assertFalse(
                refreshAccessTokenStep
                        .refreshToken(getValidOAuth2Token())
                        .isAuthenticationFinished());
    }

    @Test
    public void shouldReturnFalseWhenUserConsentInvalid() {
        when(apiClient.getDecoupledToken(Mockito.any()))
                .thenReturn(getValidTokenResponseAfterRefresh());
        when(apiClient.isConsentValid()).thenReturn(false);
        Assert.assertFalse(
                refreshAccessTokenStep
                        .refreshToken(getValidOAuth2Token())
                        .isAuthenticationFinished());
        Assert.assertEquals(
                "create_new_consent_step",
                refreshAccessTokenStep.refreshToken(getValidOAuth2Token()).getNextStepId().get());
    }

    @Test
    public void shouldReturnTrueWhenValidRefreshedTokenAndUserConsentValid() {
        when(apiClient.getDecoupledToken(Mockito.any()))
                .thenReturn(getValidTokenResponseAfterRefresh());
        when(apiClient.isConsentValid()).thenReturn(true);
        Assert.assertTrue(
                refreshAccessTokenStep
                        .refreshToken(getValidOAuth2Token())
                        .isAuthenticationFinished());
    }

    private OAuth2Token getValidOAuth2Token() {
        TokenResponse tokenResponse =
                SerializationUtils.deserializeFromString(
                        "{\"access_token\":\"accessToken1\",\"token_type\":\"Bearer\",\"refresh_token\":\"refreshToken1\",\"refresh_token_expires_in\":7775999,\"expires_in\":3599,\"scope\":\"account.lists accounts consents\"}",
                        TokenResponse.class);
        return tokenResponse.toTinkToken();
    }

    private OAuth2Token getOAuth2TokenWithoutRefreshTokenExpiry() {
        TokenResponse tokenResponse =
                SerializationUtils.deserializeFromString(
                        "{\"access_token\":\"accessToken1\",\"token_type\":\"Bearer\",\"refresh_token\":\"refreshToken1\",\"expires_in\":3599,\"scope\":\"account.lists accounts consents\"}",
                        TokenResponse.class);
        return tokenResponse.toTinkToken();
    }

    private OAuth2Token getOAuth2TokenWithoutRefreshToken() {
        TokenResponse tokenResponse =
                SerializationUtils.deserializeFromString(
                        "{\"access_token\":\"accessToken1\",\"token_type\":\"Bearer\",\"expires_in\":3599,\"scope\":\"account.lists accounts consents\"}",
                        TokenResponse.class);
        return tokenResponse.toTinkToken();
    }

    private TokenResponse getEmptyTokenResponseAfterRefresh() {
        return SerializationUtils.deserializeFromString("{}", TokenResponse.class);
    }

    private TokenResponse getTokenResponseWithAccessTokenMissingAfterRefresh() {
        return SerializationUtils.deserializeFromString(
                "{\"token_type\":\"Bearer\",\"refresh_token\":\"refreshToke2\",\"refresh_token_expires_in\":7775999,\"scope\":\"account.lists accounts consents\"}",
                TokenResponse.class);
    }

    private TokenResponse getValidTokenResponseAfterRefresh() {
        return SerializationUtils.deserializeFromString(
                "{\"access_token\":\"accessToken2\",\"token_type\":\"Bearer\",\"refresh_token\":\"refreshToke2\",\"refresh_token_expires_in\":7775999,\"expires_in\":3599,\"scope\":\"account.lists accounts consents\"}",
                TokenResponse.class);
    }

    private ErrorResponse getInvalidTokenErrorResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"type\":\"https://berlingroup.com/error-codes/TOKEN_INVALID\",\"code\":\"TOKEN_INVALID\",\"title\":\"Invalid refresh token\",\"detail\":\"https://developer.baltics.sebgroup.com/ob/docs/errors\"}",
                ErrorResponse.class);
    }
}
