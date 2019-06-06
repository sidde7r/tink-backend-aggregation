package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Constants;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OAuthUtils {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder();
    private static final Base64.Encoder BASE64 = Base64.getEncoder();
    private static final Random RANDOM = new SecureRandom();
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String AMPERSAND = "&";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String COMMA = ",";
    private static final String EQUAL_SIGN = "=";
    private static final String QUOTE_COMMA_SPACE = "\", ";
    private static final String EQUAL_QUOTE = "=\"";
    private static final String QUOTE = "\"";
    private static final char PARAMETER_SEPARATOR = ',';

    private OAuthUtils() {}

    public static String getSignature(
            String url,
            String method,
            List<NameValuePair> params,
            String consumerSecret,
            String oauthSecret)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String paramsString =
                URLEncoder.encode(URLEncodedUtils.format(params, UTF_8), UTF_8.name());

        String signingBaseString = getSigningBaseString(url, method, paramsString);
        SecretKey key = getSecretKey(consumerSecret, oauthSecret);
        byte[] macBytes = calculateMac(signingBaseString, key);
        return new String(BASE64.encode(macBytes), UTF_8).trim();
    }

    public static String getAuthorizationHeaderValue(List<NameValuePair> queryParams) {
        String params = URLEncodedUtils.format(queryParams, PARAMETER_SEPARATOR, UTF_8);
        params = params.replaceAll(COMMA, QUOTE_COMMA_SPACE);
        params = params.replaceAll(EQUAL_SIGN, EQUAL_QUOTE);
        params = params + QUOTE;
        return OAuth1Constants.QueryValues.OAUTH + StringUtils.SPACE + params;
    }

    public static String generateNonce() {
        byte[] randomData = new byte[32];
        RANDOM.nextBytes(randomData);
        return URL_ENCODER.encodeToString(randomData);
    }

    public static Map<String, String> parseFormResponse(String responseQuery) {
        Map<String, String> queryPairs = Maps.newHashMap();
        String[] pairs = responseQuery.split(AMPERSAND);
        for (String pair : pairs) {
            int idx = pair.indexOf(EQUAL_SIGN);
            queryPairs.put(
                    EncodingUtils.decodeUrl(pair.substring(0, idx)).toUpperCase(),
                    EncodingUtils.decodeUrl(pair.substring(idx + 1)));
        }
        return queryPairs;
    }

    public static String getTimestamp() {
        return StringUtils.EMPTY + System.currentTimeMillis() / 1000;
    }

    // IMPORTANT: Orders of parameters is crucial in OAuth 1 - https://oauth.net/core/1.0a/
    public static List<NameValuePair> getAccessTokenParams(
            String consumerKey, String oauthToken, String oauthVerifier) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_CONSUMER_KEY, consumerKey));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_NONCE, OAuthUtils.generateNonce()));
        params.add(
                pair(
                        OAuth1Constants.QueryParams.OAUTH_SIGNATURE_METHOD,
                        OAuth1Constants.QueryValues.HMAC_SHA1));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_TIMESTAMP, OAuthUtils.getTimestamp()));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_TOKEN, oauthToken));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_VERIFIER, oauthVerifier));
        params.add(
                pair(
                        OAuth1Constants.QueryParams.OAUTH_VERSION,
                        OAuth1Constants.QueryValues.VERSION));
        return params;
    }

    // IMPORTANT: Orders of parameters is crucial in OAuth 1 - https://oauth.net/core/1.0a/
    public static List<NameValuePair> getRequestTokenParams(
            String callbackUrl, String consumerKey) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_CALLBACK, callbackUrl));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_CONSUMER_KEY, consumerKey));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_NONCE, OAuthUtils.generateNonce()));
        params.add(
                pair(
                        OAuth1Constants.QueryParams.OAUTH_SIGNATURE_METHOD,
                        OAuth1Constants.QueryValues.HMAC_SHA1));
        params.add(pair(OAuth1Constants.QueryParams.OAUTH_TIMESTAMP, OAuthUtils.getTimestamp()));
        params.add(
                pair(
                        OAuth1Constants.QueryParams.OAUTH_VERSION,
                        OAuth1Constants.QueryValues.VERSION));
        return params;
    }

    public static String formatSupplementalKey(String key) {
        return String.format(UNIQUE_PREFIX_TPCB, key);
    }

    private static byte[] calculateMac(String signingBaseString, SecretKey key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);
        return mac.doFinal(signingBaseString.getBytes(UTF_8));
    }

    private static String getSigningBaseString(String url, String method, String paramsString)
            throws UnsupportedEncodingException {
        StringBuilder base = new StringBuilder();
        base.append(method);
        base.append(AMPERSAND);
        base.append(URLEncoder.encode(url, UTF_8.name()));
        base.append(AMPERSAND);
        base.append(paramsString);
        return base.toString();
    }

    // Get request token is signed only by consumer secret, every other request with
    // consumer&oauthSecret combination
    private static SecretKey getSecretKey(String consumerSecret, String oauthSecret) {
        String key = consumerSecret
                        + AMPERSAND
                        + StringUtils.defaultString(oauthSecret, StringUtils.EMPTY);
        return new SecretKeySpec(key.getBytes(UTF_8), HMAC_SHA1);
    }

    private static BasicNameValuePair pair(String name, String value) {
        return new BasicNameValuePair(name, value);
    }
}
