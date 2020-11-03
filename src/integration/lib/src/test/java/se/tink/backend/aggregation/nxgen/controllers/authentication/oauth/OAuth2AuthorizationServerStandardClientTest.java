package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class OAuth2AuthorizationServerStandardClientTest extends WireMockIntegrationTest {

    private static final long WAIT_TIMEOUT = 60;
    private static final TimeUnit WAIT_TIME_UNIT = TimeUnit.MINUTES;
    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";
    private static final String VALID_KEY = "tpcb_" + VALID_V4_UUID;
    private OAuth2TokenIssueStrategy codeTypeIssueStrategy;
    private OAuth2TokenIssueStrategy tokenTypeIssueStrategy;
    private OAuth2Token codeTypeIssuedToken;
    private OAuth2Token tokenTypeIssuedToken;
    private OAuth2AuthorizationSpecification authorizationSpecification;
    private OAuth2AuthorizationServerStandardClient objectUnderTest;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void init() {
        codeTypeIssueStrategy = Mockito.mock(OAuth2TokenIssueStrategy.class);
        tokenTypeIssueStrategy = Mockito.mock(OAuth2TokenIssueStrategy.class);
        codeTypeIssuedToken = Mockito.mock(OAuth2Token.class);
        tokenTypeIssuedToken = Mockito.mock(OAuth2Token.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        Mockito.when(codeTypeIssueStrategy.issueToken(Mockito.anyMap()))
                .thenReturn(codeTypeIssuedToken);
        Mockito.when(tokenTypeIssueStrategy.issueToken(Mockito.anyMap()))
                .thenReturn(tokenTypeIssuedToken);
        Mockito.when(strongAuthenticationState.getSupplementalKey()).thenReturn(VALID_KEY);
        Mockito.when(strongAuthenticationState.getState()).thenReturn(VALID_V4_UUID);
        authorizationSpecification = Mockito.mock(OAuth2AuthorizationSpecification.class);
        objectUnderTest =
                new OAuth2AuthorizationServerStandardClient(
                        httpClient,
                        authorizationSpecification,
                        WAIT_TIMEOUT,
                        WAIT_TIME_UNIT,
                        codeTypeIssueStrategy,
                        tokenTypeIssueStrategy,
                        strongAuthenticationState);
    }

    @Test
    public void shouldCallIssueTokenEndpointWhenResponseTypeIsSetToCode() {
        // given
        Mockito.when(authorizationSpecification.getResponseType()).thenReturn("code");
        Mockito.when(authorizationSpecification.isResponseTypeCode()).thenReturn(true);
        // when
        OAuth2Token result = objectUnderTest.handleAuthorizationResponse(new HashMap<>());
        // then
        Assert.assertEquals(codeTypeIssuedToken, result);
    }

    @Test
    public void shouldCreateTokenWhenResponseTypeIsSetToToken() {
        // given
        Mockito.when(authorizationSpecification.getResponseType()).thenReturn("token");
        Mockito.when(authorizationSpecification.isResponseTypeCode()).thenReturn(false);
        // when
        OAuth2Token result = objectUnderTest.handleAuthorizationResponse(new HashMap<>());
        // then
        Assert.assertEquals(tokenTypeIssuedToken, result);
    }

    @Test
    public void shouldIssuedAccessTokenUsingRefreshToken() throws MalformedURLException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"expires_in\":3600,\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String refreshTokenPath = "/refreshToken";
        final String refreshToken = "12323njmfvnlk31j54r1234";
        final String scope = "scope_example";
        Mockito.when(authorizationSpecification.getScope()).thenReturn(Optional.of(scope));
        Mockito.when(authorizationSpecification.getRefreshTokenEndpoint())
                .thenReturn(new EndpointSpecification(getOrigin() + refreshTokenPath));
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(refreshTokenPath))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(WireMock.matching(".*grant_type=refresh_token.*"))
                        .withRequestBody(
                                WireMock.matching(".*refresh_token=" + refreshToken + ".*"))
                        .withRequestBody(WireMock.matching(".*scope=" + scope + ".*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));
        // when
        OAuth2Token result = objectUnderTest.refreshAccessToken(refreshToken);
        // then
        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", result.getAccessToken());
        Assert.assertEquals("example", result.getTokenType());
        Assert.assertEquals(new Long(3600), result.getExpiresIn());
        Assert.assertEquals("tGzv3JOkF0XG5Qx2TlKWIA", result.getRefreshToken());
    }

    @Test
    public void shouldIssuedAccessTokenUsingRefreshTokenWithClientSpecificParams()
            throws MalformedURLException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"expires_in\":3600,\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String refreshTokenPath = "/refreshToken";
        final String refreshToken = "12323njmfvnlk31j54r1234";
        final String scope = "scope_example";
        Mockito.when(authorizationSpecification.getScope()).thenReturn(Optional.of(scope));
        Mockito.when(authorizationSpecification.getRefreshTokenEndpoint())
                .thenReturn(
                        new EndpointSpecification(getOrigin() + refreshTokenPath)
                                .withClientSpecificParameter(
                                        "specificParamKey", "specificParamValue")
                                .withHeader("specificHeaderKey", "specificHeaderValue"));
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(refreshTokenPath))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withHeader("specificHeaderKey", WireMock.equalTo("specificHeaderValue"))
                        .withRequestBody(WireMock.matching(".*grant_type=refresh_token.*"))
                        .withRequestBody(
                                WireMock.matching(".*refresh_token=" + refreshToken + ".*"))
                        .withRequestBody(WireMock.matching(".*scope=" + scope + ".*"))
                        .withRequestBody(WireMock.matching(".*specificParamKey=specificParamValue"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));
        // when
        OAuth2Token result = objectUnderTest.refreshAccessToken(refreshToken);
        // then
        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", result.getAccessToken());
        Assert.assertEquals("example", result.getTokenType());
        Assert.assertEquals(new Long(3600), result.getExpiresIn());
        Assert.assertEquals("tGzv3JOkF0XG5Qx2TlKWIA", result.getRefreshToken());
    }

    @Test
    public void shouldIssuedAccessTokenUsingRefreshTokenNewRefreshTokenIsNotReturned()
            throws MalformedURLException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"expires_in\":3600}";
        final String refreshTokenPath = "/refreshToken";
        final String refreshToken = "12323njmfvnlk31j54r1234";
        final String scope = "scope_example";
        Mockito.when(authorizationSpecification.getScope()).thenReturn(Optional.of(scope));
        Mockito.when(authorizationSpecification.getRefreshTokenEndpoint())
                .thenReturn(new EndpointSpecification(getOrigin() + refreshTokenPath));
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(refreshTokenPath))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(WireMock.matching(".*grant_type=refresh_token.*"))
                        .withRequestBody(
                                WireMock.matching(".*refresh_token=" + refreshToken + ".*"))
                        .withRequestBody(WireMock.matching(".*scope=" + scope + ".*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));
        // when
        OAuth2Token result = objectUnderTest.refreshAccessToken(refreshToken);
        // then
        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", result.getAccessToken());
        Assert.assertEquals("example", result.getTokenType());
        Assert.assertEquals(new Long(3600), result.getExpiresIn());
        Assert.assertEquals(refreshToken, result.getRefreshToken());
    }

    @Test
    public void shouldReturnProperThirdPartyAppPayload()
            throws MalformedURLException, UnsupportedEncodingException {
        // given
        final String authorizationPath = "/authorizationPath";
        final String authorizationEndpoint = getOrigin() + authorizationPath;
        final String responseType = "code";
        final String clientId = "clientId";
        final String redirectUrl = "http://127.0.0.1/callbackHandler";
        final String scope = "example_scope";
        Mockito.when(authorizationSpecification.getAuthorizationEndpoint())
                .thenReturn(new EndpointSpecification(authorizationEndpoint));
        Mockito.when(authorizationSpecification.getResponseType()).thenReturn(responseType);
        Mockito.when(authorizationSpecification.getClientId()).thenReturn(clientId);
        Mockito.when(authorizationSpecification.getRedirectUrl()).thenReturn(new URL(redirectUrl));
        Mockito.when(authorizationSpecification.getScope()).thenReturn(Optional.of(scope));
        // when
        ThirdPartyAppAuthenticationPayload result =
                objectUnderTest.getAuthorizationEndpointPayload();
        /// then
        final URL authorizationUrl = new URL(result.getDesktop().getUrl());
        final String query = authorizationUrl.getQuery();
        Assert.assertEquals(authorizationPath, authorizationUrl.getPath());
        Assert.assertTrue(
                query.contains("response_type=" + responseType)
                        && query.contains("client_id=" + clientId)
                        && query.contains("redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8"))
                        && query.contains("scope=" + scope)
                        && query.contains("state=" + VALID_V4_UUID));
    }

    @Test
    public void shouldReturnProperWaitingForThirdPartyRedirectConfiguration() {
        // when
        SupplementalWaitRequest result = objectUnderTest.getWaitingForResponseConfiguration();
        // then
        Assert.assertEquals(WAIT_TIME_UNIT, result.getTimeUnit());
        Assert.assertEquals(WAIT_TIMEOUT, result.getWaitFor());
        Assert.assertEquals(VALID_KEY, result.getKey());
    }

    @Test
    public void shouldThrowExceptionWhenAuthorizationServerRespondWithError() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("error", "invalid_scope");
        callbackData.put("error_description", "description of invalid scope");
        // when
        Throwable thrown =
                Assertions.catchThrowable(
                        () -> objectUnderTest.handleAuthorizationResponse(callbackData));
        // then
        Assertions.assertThat(thrown).isInstanceOf(OAuth2AuthorizationException.class);
        OAuth2AuthorizationException authorizationException = (OAuth2AuthorizationException) thrown;
        Assert.assertEquals(
                OAuth2AuthorizationErrorType.INVALID_SCOPE, authorizationException.getErrorType());
        Assert.assertEquals(callbackData.get("error"), authorizationException.getErrorRawCode());
        Assert.assertEquals(
                callbackData.get("error_description"), authorizationException.getDescription());
    }
}
