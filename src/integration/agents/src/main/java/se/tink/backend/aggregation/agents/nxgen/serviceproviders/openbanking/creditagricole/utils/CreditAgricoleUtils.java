package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.NameValuePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;

public class CreditAgricoleUtils {
    public static final String FORMAT = "<%1$s>(.+?)</%1$s>";
    public static Function<String, String> TAG_STRING_GETTER = tag -> String.format(FORMAT, tag);
    public static Function<String, Pattern> TAG_PATTERN_GETTER =
            tag -> Pattern.compile(tag, Pattern.DOTALL);

    public static List<NameValuePair> getUserIdRequestParams(
            String oauthToken, String consumerKey) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(OAuthUtils.pair(OAuth1Constants.QueryParams.OAUTH_CONSUMER_KEY, consumerKey));
        params.add(
                OAuthUtils.pair(
                        OAuth1Constants.QueryParams.OAUTH_NONCE, OAuthUtils.generateNonce()));
        params.add(
                OAuthUtils.pair(
                        OAuth1Constants.QueryParams.OAUTH_SIGNATURE_METHOD,
                        OAuth1Constants.QueryValues.HMAC_SHA1));
        params.add(
                OAuthUtils.pair(
                        OAuth1Constants.QueryParams.OAUTH_TIMESTAMP, OAuthUtils.getTimestamp()));
        params.add(OAuthUtils.pair(OAuth1Constants.QueryParams.OAUTH_TOKEN, oauthToken));
        params.add(
                OAuthUtils.pair(
                        OAuth1Constants.QueryParams.OAUTH_VERSION,
                        OAuth1Constants.QueryValues.VERSION));
        return params;
    }

    public static String getXMLResponse(String tag, String xml) {
        Pattern pattern = TAG_STRING_GETTER.andThen(TAG_PATTERN_GETTER).apply(tag);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException(
                    String.format("No matching tag <%s> in xml response", tag));
        }
    }
}
