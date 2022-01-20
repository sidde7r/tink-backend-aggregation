package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupAuthenticatorTest {

    private static final String EXCHANGE_CODE = "exchange_code";
    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "2345";
    private static final String TOKEN_TYPE = "Bearer";
    private static final long TOKEN_EXPIRES_IN = 3600L;

    private BpceGroupAuthenticator bpceGroupAuthenticator;

    private BpceGroupApiClient apiClientMock;

    @Before
    public void setUp() {
        apiClientMock = mock(BpceGroupApiClient.class);

        when(apiClientMock.exchangeAuthorizationToken(EXCHANGE_CODE))
                .thenReturn(getTokenResponse());
        when(apiClientMock.exchangeRefreshToken(EXCHANGE_CODE)).thenReturn(getTokenResponse());

        final BpceOAuth2TokenStorage oAuth2TokenStorageMock = mock(BpceOAuth2TokenStorage.class);

        bpceGroupAuthenticator = new BpceGroupAuthenticator(apiClientMock, oAuth2TokenStorageMock);
    }

    @Test
    public void shouldExchangeAuthorizationCode() {
        // when
        final OAuth2Token returnedToken =
                bpceGroupAuthenticator.exchangeAuthorizationCode(EXCHANGE_CODE);

        // then
        assertTokenIsValid(returnedToken);
    }

    @Test
    public void shouldThrowExceptionWhenHttpResponseExceptionOccurs() {
        // given
        final HttpResponseException httpResponseException = createHttpResponseException();
        when(apiClientMock.exchangeAuthorizationToken(anyString()))
                .thenThrow(httpResponseException);

        // when
        final Throwable thrown =
                catchThrowable(
                        () -> bpceGroupAuthenticator.exchangeAuthorizationCode(EXCHANGE_CODE));

        // then
        assertThat(thrown).isExactlyInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldRefreshAccessToken() throws SessionException {
        // when
        final OAuth2Token returnedToken = bpceGroupAuthenticator.refreshAccessToken(EXCHANGE_CODE);

        // then
        assertTokenIsValid(returnedToken);
    }

    private static void assertTokenIsValid(OAuth2Token token) {
        assertNotNull(token);
        assertTrue(token.isValid());
        assertFalse(token.isRefreshTokenExpirationPeriodSpecified());
        assertTrue(token.canUseAccessToken());
        assertTrue(token.canRefresh());
        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(token.getTokenType()).isEqualTo(TOKEN_TYPE);
        assertTrue(token.getOptionalRefreshToken().isPresent());
        assertThat(token.getOptionalRefreshToken().get()).isEqualTo(REFRESH_TOKEN);
    }

    private static HttpResponseException createHttpResponseException() {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);

        return new HttpResponseException(httpRequest, httpResponse);
    }

    private static TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"access_token\":\""
                        + ACCESS_TOKEN
                        + "\",\n"
                        + "\"token_type\":\""
                        + TOKEN_TYPE
                        + "\",\n"
                        + "\"expires_in\":"
                        + TOKEN_EXPIRES_IN
                        + ",\n"
                        + "\"refresh_token\":\""
                        + REFRESH_TOKEN
                        + "\",\n"
                        + "\"scope\":\"xx\",\n"
                        + "\"state\":\"abc\"\n"
                        + "}",
                TokenResponse.class);
    }
}
