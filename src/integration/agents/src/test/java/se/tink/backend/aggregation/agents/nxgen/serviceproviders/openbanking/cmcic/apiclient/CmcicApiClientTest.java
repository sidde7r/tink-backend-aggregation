package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.TOKEN_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.BENEFICIARIES_2ND_PAGE_PATH;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicCodeChallengeProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicDigestProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
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

    private static final String ORG_NAME = "Tink AB";
    private static final String BASE_URL = "https://cmcic.fr";
    private static final String BASE_PATH = "/psd2/";
    private static final String TOKEN_PATH = "oauth2/token";
    private static final String TOKEN_URL = BASE_URL + BASE_PATH + TOKEN_PATH;
    private static final String BENEFICIARIES_PATH = "trusted-beneficiaries";
    private static final String API_PATH = "stet-psd2-api/v2.0/";
    private static final String PAYMENT_ID = "unique-id-2137";
    private static final String PAYMENT_PATH = "payment-requests/";
    private static final String PAYMENT_CONFIRMATION_PATH = "/o-confirmation";
    private static final String PAYMENT_URL =
            BASE_URL + BASE_PATH + API_PATH + PAYMENT_PATH + PAYMENT_ID + PAYMENT_CONFIRMATION_PATH;
    private static final String BENEFICIARIES_URL =
            BASE_URL + BASE_PATH + API_PATH + BENEFICIARIES_PATH;

    private CmcicApiClient cmcicApiClient;
    private TinkHttpClient tinkHttpClientMock;

    @Before
    public void setUp() {
        final SessionStorage sessionStorageMock = mock(SessionStorage.class);
        final PersistentStorage persistentStorageMock = createPersistentStorageMock();
        cmcicApiClient = setupCmcicApiClient(persistentStorageMock, sessionStorageMock);
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

        final String path = BASE_PATH + BASE_API_PATH + BENEFICIARIES_2ND_PAGE_PATH;

        final String url = BASE_URL + path;
        setUpHttpClientMockForApi(url, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                cmcicApiClient.getTrustedBeneficiaries(path);

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

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenNoAisTokenFound() {
        // given
        SessionStorage sessionStorage = new SessionStorage();
        PersistentStorage persistentStorageWithoutAisToken = new PersistentStorage();
        cmcicApiClient = setupCmcicApiClient(persistentStorageWithoutAisToken, sessionStorage);

        // expect
        assertThatThrownBy(
                        () -> {
                            cmcicApiClient.fetchAccounts();
                        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SESSION_EXPIRED");
    }

    @Test
    public void shouldNotThrowExceptionWhenConfirmingPaymentAndPisTokenIsPresent() {
        // given
        SessionStorage sessionStorageWithPisToken = new SessionStorage();
        sessionStorageWithPisToken.put("PISP_TOKEN", anyValidOauth2Token());
        PersistentStorage persistentStorage = new PersistentStorage();
        cmcicApiClient = setupCmcicApiClient(persistentStorage, sessionStorageWithPisToken);
        setUpHttpClientMockForApi(PAYMENT_URL, mock(HttpResponse.class));

        // expect
        assertThatCode(() -> cmcicApiClient.confirmPayment(PAYMENT_ID)).doesNotThrowAnyException();
    }

    private OAuth2Token anyValidOauth2Token() {
        return new OAuth2Token(
                "oauth2",
                "accessToken",
                "refreshToken",
                "2137",
                180000,
                180000,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    private CmcicApiClient setupCmcicApiClient(
            PersistentStorage persistentStorage, SessionStorage sessionStorage) {
        tinkHttpClientMock = mock(TinkHttpClient.class);
        final CmcicDigestProvider digestProviderMock = mock(CmcicDigestProvider.class);
        final CmcicSignatureProvider signatureProviderMock = mock(CmcicSignatureProvider.class);
        final CmcicCodeChallengeProvider codeChallengeProviderMock =
                mock(CmcicCodeChallengeProvider.class);
        final CmcicAgentConfig cmcicAgentConfig = createCmcicAgentConfigurationMock();
        AgentConfiguration<CmcicConfiguration> agentConfigurationMock =
                mock(AgentConfiguration.class);

        CmcicConfiguration cmcicConfigurationMock = createCmcicConfigurationMock();
        when(agentConfigurationMock.getProviderSpecificConfiguration())
                .thenReturn(cmcicConfigurationMock);

        return cmcicApiClient =
                new CmcicApiClient(
                        tinkHttpClientMock,
                        new CmcicRepository(persistentStorage, sessionStorage),
                        agentConfigurationMock,
                        digestProviderMock,
                        signatureProviderMock,
                        codeChallengeProviderMock,
                        cmcicAgentConfig,
                        new CmcicRequestValuesProvider(
                                new RandomValueGeneratorImpl(),
                                new ActualLocalDateTimeSource(),
                                EIdasTinkCert.QSEALC));
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
        assertFalse(token.isRefreshTokenExpirationPeriodSpecified());
        assertTrue(token.canUseAccessToken());
        assertTrue(token.canRefresh());
        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(token.getTokenType()).isEqualTo(TOKEN_TYPE);
        assertTrue(token.getOptionalRefreshToken().isPresent());
        assertThat(token.getOptionalRefreshToken().get()).isEqualTo(REFRESH_TOKEN);
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
        when(requestBuilderMock.header(HeaderKeys.USER_AGENT, ORG_NAME))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);
        when(requestBuilderMock.type(anyString())).thenReturn(requestBuilderMock);

        when(tinkHttpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);
    }

    private static CmcicConfiguration createCmcicConfigurationMock() {
        final CmcicConfiguration cmcicConfigurationMock = mock(CmcicConfiguration.class);

        when(cmcicConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        return cmcicConfigurationMock;
    }

    private static CmcicAgentConfig createCmcicAgentConfigurationMock() {
        final CmcicAgentConfig agentConfig = mock(CmcicAgentConfig.class);

        when(agentConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(agentConfig.getBasePath()).thenReturn(BASE_PATH);

        return agentConfig;
    }

    private static PersistentStorage createPersistentStorageMock() {
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        final OAuth2Token oAuth2TokenMock = mock(OAuth2Token.class);

        when(persistentStorageMock.get(CmcicConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2TokenMock));

        return persistentStorageMock;
    }
}
