package se.tink.backend.aggregation.agents.banks.norwegian.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginParsingUtils {

    public static String getRedirectUrl(String body) {
        final Pattern pattern =
                Pattern.compile("window\\.(?:parent\\.*)location\\.href\\s*=\\s*['\"](.+)['\"];");

        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Couldn't find JS redirect URL. HTML changed?");
        }
    }
}
