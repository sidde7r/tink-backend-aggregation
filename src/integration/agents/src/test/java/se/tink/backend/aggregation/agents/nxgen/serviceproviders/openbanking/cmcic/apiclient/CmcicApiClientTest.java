package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicCodeChallengeProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicDigestProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicSignatureProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CmcicApiClientTest {

    private static final String BASE_URL = "https://base-url";
    private static final String BASE_PATH = "/base-path/";
    private static final String TOKEN_PATH = "oauth2/token";
    private static final String TOKEN_URL = BASE_URL + BASE_PATH + TOKEN_PATH;
    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "4321";
    private static final String TOKEN_TYPE = "bearer";
    private static final String CLIENT_ID = "cid";
    private static final long TOKEN_EXPIRES_IN = 3600L;

    private CmcicApiClient cmcicApiClient;
    private TinkHttpClient tinkHttpClientMock;

    @Before
    public void setUp() {
        tinkHttpClientMock = mock(TinkHttpClient.class);
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        final SessionStorage sessionStorageMock = mock(SessionStorage.class);

        final CmcicConfiguration cmcicConfigurationMock = mock(CmcicConfiguration.class);
        when(cmcicConfigurationMock.getBaseUrl()).thenReturn(BASE_URL);
        when(cmcicConfigurationMock.getBasePath()).thenReturn(BASE_PATH);
        when(cmcicConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        final CmcicDigestProvider digestProviderMock = mock(CmcicDigestProvider.class);
        final CmcicSignatureProvider signatureProviderMock = mock(CmcicSignatureProvider.class);
        final CmcicCodeChallengeProvider codeChallengeProviderMock =
                mock(CmcicCodeChallengeProvider.class);

        cmcicApiClient =
                new CmcicApiClient(
                        tinkHttpClientMock,
                        persistentStorageMock,
                        sessionStorageMock,
                        cmcicConfigurationMock,
                        digestProviderMock,
                        signatureProviderMock,
                        codeChallengeProviderMock);
    }

    @Test
    public void shouldRefreshAccessToken() throws SessionException {
        // given
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final TokenResponse tokenResponse = getTokenResponse();
        when(requestBuilderMock.post(TokenResponse.class)).thenReturn(tokenResponse);

        final ArgumentCaptor<RefreshTokenTokenRequest> refreshTokenTokenRequestCaptor =
                ArgumentCaptor.forClass(RefreshTokenTokenRequest.class);
        when(requestBuilderMock.body(refreshTokenTokenRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final OAuth2Token response = cmcicApiClient.refreshToken(REFRESH_TOKEN);

        // then
        assertTokenIsValid(response);

        final String expectedRequestString = getRefreshRequest();
        final RefreshTokenTokenRequest actualRequest = refreshTokenTokenRequestCaptor.getValue();
        assertThat(actualRequest.getBodyValue()).isEqualTo(expectedRequestString);
    }

    @Test
    public void shouldThrowSessionExceptionWhenRefreshTokenIsExpired() {
        // given
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        when(requestBuilderMock.body(any(), anyString())).thenReturn(requestBuilderMock);

        final HttpResponseException httpResponseExceptionMock = mock(HttpResponseException.class);
        when(httpResponseExceptionMock.getMessage())
                .thenReturn(
                        "Response statusCode: 400 with body: {\"error\":\"invalid_grant\",\"error_description\":\"Refresh token has expired.\",\"error_uri\":null}");

        when(requestBuilderMock.post(TokenResponse.class)).thenThrow(httpResponseExceptionMock);

        // when
        final Throwable thrown = catchThrowable(() -> cmcicApiClient.refreshToken(REFRESH_TOKEN));

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    private RequestBuilder setUpHttpClientMockForAuth() {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);
        when(requestBuilderMock.type(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(tinkHttpClientMock.request(new URL(TOKEN_URL))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
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

    private static void assertTokenIsValid(OAuth2Token token) {
        assertNotNull(token);
        assertTrue(token.isValid());
        assertFalse(token.hasRefreshExpire());
        assertFalse(token.hasAccessExpired());
        assertTrue(token.canRefresh());
        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(token.getTokenType()).isEqualTo(TOKEN_TYPE);
        assertTrue(token.getRefreshToken().isPresent());
        assertThat(token.getRefreshToken().get()).isEqualTo(REFRESH_TOKEN);
    }

    private static String getRefreshRequest() {
        return String.format(
                "client_id=%s&grant_type=refresh_token&refresh_token=%s", CLIENT_ID, REFRESH_TOKEN);
    }
}
