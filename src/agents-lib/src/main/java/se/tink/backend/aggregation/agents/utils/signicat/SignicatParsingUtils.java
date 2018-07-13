package se.tink.backend.aggregation.agents.utils.signicat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignicatParsingUtils {

    private static final Pattern AUTHENTICATION_BANKID_URL_PATTERN = Pattern.compile(
            ".*?(signicat\\.serviceUrl).*?(\\'.*?\\')", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Parses the signicat service url from the html contents of a web page
     */
    public static String parseBankIdServiceUrl(String htmlContent) {

        Matcher matcher = AUTHENTICATION_BANKID_URL_PATTERN.matcher(htmlContent);

        if (matcher.find()) {
            return matcher.group(2).replace("'", "");
        }

        return null;
    }

}
