package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class OAuth2CodeTypeTokenIssueStrategyTest extends WireMockIntegrationTest {

    private static final String ACCESS_TOKEN_PATH = "/accessToken";
    private OAuth2CodeTypeTokenIssueStrategy objectUnderTest;
    private OAuth2AuthorizationSpecification authorizationParamProvider;

    @Before
    public void init() {
        this.authorizationParamProvider = Mockito.mock(OAuth2AuthorizationSpecification.class);
        try {
            Mockito.when(authorizationParamProvider.getAccessTokenEndpoint())
                    .thenReturn(
                            new EndpointSpecification(new URL(getOrigin() + ACCESS_TOKEN_PATH)));
            objectUnderTest =
                    new OAuth2CodeTypeTokenIssueStrategy(authorizationParamProvider, httpClient);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailedWhenCallbackDataDoNotContainCodeParam() {
        // when
        objectUnderTest.issueToken(new HashMap<>());
    }

    @Test
    public void shouldIssueAccessToken()
            throws MalformedURLException, UnsupportedEncodingException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"expires_in\":3600,\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String code = "12345678";
        Map<String, String> accessTokenClientSpecificParams = new HashMap();
        accessTokenClientSpecificParams.put("specificParamKey", "specificParamValue");
        Mockito.when(authorizationParamProvider.getClientId()).thenReturn("clientId");
        Mockito.when(authorizationParamProvider.getRedirectUrl())
                .thenReturn(new URL("http://127.0.0.1/redirectUrl"));
        Mockito.when(authorizationParamProvider.getAccessTokenRequestClientSpecificParameters())
                .thenReturn(accessTokenClientSpecificParams);

        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(ACCESS_TOKEN_PATH))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withRequestBody(WireMock.matching(".*grant_type=authorization_code.*"))
                        .withRequestBody(WireMock.matching(".*code=" + code + ".*"))
                        .withRequestBody(
                                WireMock.matching(
                                        ".*redirect_uri="
                                                + URLEncoder.encode(
                                                        authorizationParamProvider
                                                                        .getRedirectUrl()
                                                                        .toString()
                                                                + ".*",
                                                        "UTF-8")))
                        .withRequestBody(
                                WireMock.matching(
                                        ".*client_id="
                                                + authorizationParamProvider.getClientId()
                                                + ".*"))
                        .withRequestBody(
                                WireMock.matching(".*specificParamKey=specificParamValue.*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));

        Map<String, String> callbackData = new HashMap();
        callbackData.put("code", code);
        // when
        OAuth2Token result = objectUnderTest.issueToken(callbackData);
        // then
        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", result.getAccessToken());
        Assert.assertEquals("example", result.getTokenType());
        Assert.assertEquals(new Long(3600), result.getExpiresIn());
        Assert.assertEquals("tGzv3JOkF0XG5Qx2TlKWIA", result.getRefreshToken());
    }

    @Test
    public void shouldIssueAccessTokeWithClientSpecificHeadersInRequest()
            throws MalformedURLException, UnsupportedEncodingException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"expires_in\":3600,\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String code = "12345678";
        Map<String, String> accessTokenClientSpecificParams = new HashMap();
        accessTokenClientSpecificParams.put("specificParamKey", "specificParamValue");
        Mockito.when(authorizationParamProvider.getClientId()).thenReturn("clientId");
        Mockito.when(authorizationParamProvider.getRedirectUrl())
                .thenReturn(new URL("http://127.0.0.1/redirectUrl"));
        Mockito.when(authorizationParamProvider.getAccessTokenRequestClientSpecificParameters())
                .thenReturn(accessTokenClientSpecificParams);
        Mockito.when(authorizationParamProvider.getAccessTokenEndpoint())
                .thenReturn(
                        new EndpointSpecification(new URL(getOrigin() + ACCESS_TOKEN_PATH))
                                .withHeader("clientHeader", "value"));

        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(ACCESS_TOKEN_PATH))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .withHeader("clientHeader", WireMock.equalTo("value"))
                        .withRequestBody(WireMock.matching(".*grant_type=authorization_code.*"))
                        .withRequestBody(WireMock.matching(".*code=" + code + ".*"))
                        .withRequestBody(
                                WireMock.matching(
                                        ".*redirect_uri="
                                                + URLEncoder.encode(
                                                        authorizationParamProvider
                                                                        .getRedirectUrl()
                                                                        .toString()
                                                                + ".*",
                                                        "UTF-8")))
                        .withRequestBody(
                                WireMock.matching(
                                        ".*client_id="
                                                + authorizationParamProvider.getClientId()
                                                + ".*"))
                        .withRequestBody(
                                WireMock.matching(".*specificParamKey=specificParamValue.*"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));

        Map<String, String> callbackData = new HashMap();
        callbackData.put("code", code);
        // when
        OAuth2Token result = objectUnderTest.issueToken(callbackData);
        // then
        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", result.getAccessToken());
        Assert.assertEquals("example", result.getTokenType());
        Assert.assertEquals(new Long(3600), result.getExpiresIn());
        Assert.assertEquals("tGzv3JOkF0XG5Qx2TlKWIA", result.getRefreshToken());
    }

    @Test
    public void shouldUseDefaultAccessTokenLifetimeWhenItIsNotProvidedByAuthorizationRequest()
            throws MalformedURLException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String code = "12345678";
        Mockito.when(authorizationParamProvider.getClientId()).thenReturn("clientId");
        Mockito.when(authorizationParamProvider.getRedirectUrl())
                .thenReturn(new URL("http://127.0.0.1/redirectUrl"));
        Mockito.when(authorizationParamProvider.getDefaultAccessTokenLifetime())
                .thenReturn(Optional.of(3600l));
        objectUnderTest =
                new OAuth2CodeTypeTokenIssueStrategy(authorizationParamProvider, httpClient);
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(ACCESS_TOKEN_PATH))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));

        Map<String, String> callbackData = new HashMap();
        callbackData.put("code", code);
        // when
        OAuth2Token token = objectUnderTest.issueToken(callbackData);
        // then
        Assert.assertEquals(
                authorizationParamProvider.getDefaultAccessTokenLifetime().get(),
                token.getExpiresIn());
    }

    @Test(expected = RuntimeException.class)
    public void
            shouldThrowRuntimeExceptionWhenAccessTokenLifetimeIsNotPresentNeitherInAuthorizationResponseNeitherInAuthorizationParamsProvider()
                    throws MalformedURLException {
        // given
        final String accessTokenResponse =
                "{\"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\"token_type\":\"example\",\"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\"}";
        final String code = "12345678";
        Mockito.when(authorizationParamProvider.getClientId()).thenReturn("clientId");
        Mockito.when(authorizationParamProvider.getRedirectUrl())
                .thenReturn(new URL("http://127.0.0.1/redirectUrl"));
        Mockito.when(authorizationParamProvider.getDefaultAccessTokenLifetime())
                .thenReturn(Optional.empty());

        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(ACCESS_TOKEN_PATH))
                        .withHeader(
                                "Content-Type",
                                WireMock.equalTo("application/x-www-form-urlencoded"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(accessTokenResponse)
                                        .withHeader("Content-Type", "application/json")));

        Map<String, String> callbackData = new HashMap();
        callbackData.put("code", code);
        // when
        objectUnderTest.issueToken(callbackData);
    }
}
