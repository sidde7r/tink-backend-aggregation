package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls.BASE_API_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.BENEFICIARIES_2ND_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.TOKEN_TYPE;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicCodeChallengeProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicDigestProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicSignatureProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
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
    private static final String BENEFICIARIES_PATH = "/trusted-beneficiaries";
    private static final String BENEFICIARIES_URL =
            BASE_URL + BASE_PATH + BASE_API_PATH + BENEFICIARIES_PATH;

    private CmcicApiClient cmcicApiClient;
    private TinkHttpClient tinkHttpClientMock;

    @Before
    public void setUp() {
        tinkHttpClientMock = mock(TinkHttpClient.class);

        final PersistentStorage persistentStorageMock = createPersistentStorageMock();
        final SessionStorage sessionStorageMock = mock(SessionStorage.class);
        final CmcicConfiguration cmcicConfigurationMock = createCmcicConfigurationMock();
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

    @Test
    public void shouldGetBeneficiariesFirstPage() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        final TrustedBeneficiariesResponseDto expectedResponse =
                mock(TrustedBeneficiariesResponseDto.class);
        when(httpResponseMock.getBody(TrustedBeneficiariesResponseDto.class))
                .thenReturn(expectedResponse);
        when(httpResponseMock.getStatus()).thenReturn(200);

        setUpHttpClientMockForApi(BENEFICIARIES_URL, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                cmcicApiClient.getTrustedBeneficiaries();

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        assertThat(returnedResponse.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetBeneficiariesSecondPage() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        final TrustedBeneficiariesResponseDto expectedResponse =
                mock(TrustedBeneficiariesResponseDto.class);
        when(httpResponseMock.getBody(TrustedBeneficiariesResponseDto.class))
                .thenReturn(expectedResponse);
        when(httpResponseMock.getStatus()).thenReturn(200);

        final String url = BASE_URL + BASE_PATH + BASE_API_PATH + BENEFICIARIES_2ND_PAGE_PATH;
        setUpHttpClientMockForApi(url, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                cmcicApiClient.getTrustedBeneficiaries(BENEFICIARIES_2ND_PAGE_PATH);

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        assertThat(returnedResponse.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldReturnEmptyWhenNoTrustedBeneficiariesExist() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getStatus()).thenReturn(204);

        setUpHttpClientMockForApi(BENEFICIARIES_URL, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                cmcicApiClient.getTrustedBeneficiaries();

        // then
        assertThat(returnedResponse.isPresent()).isFalse();
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

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.addBearerToken(any(OAuth2Token.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(eq(CmcicConstants.HeaderKeys.HOST), any()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(CmcicConstants.HeaderKeys.DATE), any()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(CmcicConstants.HeaderKeys.SIGNATURE), any()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(CmcicConstants.HeaderKeys.DIGEST), any()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(CmcicConstants.HeaderKeys.X_REQUEST_ID), any()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        CmcicConstants.HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);

        when(tinkHttpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);
    }

    private static CmcicConfiguration createCmcicConfigurationMock() {
        final CmcicConfiguration cmcicConfigurationMock = mock(CmcicConfiguration.class);

        when(cmcicConfigurationMock.getBaseUrl()).thenReturn(BASE_URL);
        when(cmcicConfigurationMock.getBasePath()).thenReturn(BASE_PATH);
        when(cmcicConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        return cmcicConfigurationMock;
    }

    private static PersistentStorage createPersistentStorageMock() {
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        final OAuth2Token oAuth2TokenMock = mock(OAuth2Token.class);

        when(persistentStorageMock.get(CmcicConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2TokenMock));

        return persistentStorageMock;
    }
}
