package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OAuthUtilsTest {

    private static final String REQUEST_RESPONSE = "oauth_token=TOKEN&oauth_verifier=VERIFIER";
    private static final String URL = "https://www.tink.se";
    private static final String CONSUMER_SECRET = "consumerSecret";
    private static final String CALLBACK_URL = "https://www.tink.se/?state=UNIQUE_STATE";
    private static final String OAUTH_SECRET = "oauthSecret";
    private static final String OAUTH_VERIFIER = "oauthVerifier";
    private static final String NOT_RANDOM_VALUE = "1";
    private static final String EXPECTED_GET_REQUEST_TOKEN_SIGNATURE =
            "bF5COChSE2Ywn14Usjd5hesZcWE=";
    private static final String EXPECTED_GET_ACCESS_TOKEN_SIGNATURE =
            "uhk++maTbT8UbObG7b36aWxCn0M=";

    @Test
    public void shouldGenerateSignatureForRequestTokenWithEmptyOAuthSecret() throws Exception {
        List<NameValuePair> params =
                OAuthUtils.getRequestTokenParams(CALLBACK_URL, CONSUMER_SECRET);

        params = replaceRandomParamsWithNoRandom(params);

        String signature =
                OAuthUtils.getSignature(URL, HttpMethod.POST.name(), params, CONSUMER_SECRET, "");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_GET_REQUEST_TOKEN_SIGNATURE);
    }

    @Test
    public void shouldGenerateSignatureForRequestTokenWithNullOauthSecret() throws Exception {
        List<NameValuePair> params =
                OAuthUtils.getRequestTokenParams(CALLBACK_URL, CONSUMER_SECRET);

        params = replaceRandomParamsWithNoRandom(params);

        String signature =
                OAuthUtils.getSignature(URL, HttpMethod.POST.name(), params, CONSUMER_SECRET, null);

        Assertions.assertThat(signature).isEqualTo(EXPECTED_GET_REQUEST_TOKEN_SIGNATURE);
    }

    @Test
    public void shouldGenerateSignatureForAccessToken() throws Exception {
        List<NameValuePair> params =
                OAuthUtils.getAccessTokenParams(CALLBACK_URL, CONSUMER_SECRET, OAUTH_VERIFIER);

        params = replaceRandomParamsWithNoRandom(params);

        String signature =
                OAuthUtils.getSignature(
                        URL, HttpMethod.POST.name(), params, CONSUMER_SECRET, OAUTH_SECRET);

        Assertions.assertThat(signature).isEqualTo(EXPECTED_GET_ACCESS_TOKEN_SIGNATURE);
    }

    @Test
    public void shouldAppendSuffixToSupplementalInformationKey() {
        Assertions.assertThat(OAuthUtils.formatSupplementalKey("raw")).isEqualTo("tpcb_raw");
    }

    @Test
    public void shouldExtractTokenAndVerifierFromResponse() {
        Map<String, String> parsedResponse = OAuthUtils.parseFormResponse(REQUEST_RESPONSE);

        Assertions.assertThat(
                        parsedResponse.get(OAuth1Constants.QueryParams.OAUTH_TOKEN.toUpperCase()))
                .isEqualTo("TOKEN");
        Assertions.assertThat(
                        parsedResponse.get(
                                OAuth1Constants.QueryParams.OAUTH_VERIFIER.toUpperCase()))
                .isEqualTo("VERIFIER");
    }

    @Test
    public void shouldGenerateTimestamp() {
        Long timestamp = Long.parseLong(OAuthUtils.getTimestamp());
        Assertions.assertThat(timestamp).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    public void shouldGenerateNonce() {
        Assertions.assertThat(OAuthUtils.generateNonce()).isNotEqualTo(OAuthUtils.generateNonce());
    }

    private List<NameValuePair> replaceRandomParamsWithNoRandom(List<NameValuePair> params) {
        BasicNameValuePair NOT_RANDOM_NONCE =
                new BasicNameValuePair(OAuth1Constants.QueryParams.OAUTH_NONCE, NOT_RANDOM_VALUE);
        BasicNameValuePair NOT_RANDOM_TIMESTAMP =
                new BasicNameValuePair(
                        OAuth1Constants.QueryParams.OAUTH_TIMESTAMP, NOT_RANDOM_VALUE);

        params =
                params.stream()
                        .map(
                                p ->
                                        p.getName().equals(OAuth1Constants.QueryParams.OAUTH_NONCE)
                                                ? p
                                                : NOT_RANDOM_NONCE)
                        .map(
                                p ->
                                        p.getName()
                                                        .equals(
                                                                OAuth1Constants.QueryParams
                                                                        .OAUTH_TIMESTAMP)
                                                ? p
                                                : NOT_RANDOM_TIMESTAMP)
                        .collect(Collectors.toList());
        return params;
    }
}
