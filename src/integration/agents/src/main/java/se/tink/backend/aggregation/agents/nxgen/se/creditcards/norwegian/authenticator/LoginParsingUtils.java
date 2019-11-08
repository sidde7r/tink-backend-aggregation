package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.ErrorMessages;

public class LoginParsingUtils {

    public static String getRedirectUrl(String body) throws LoginException {
        final Pattern pattern =
                Pattern.compile("window\\.(?:parent\\.*)location\\.href\\s*=\\s*['\"](.+)['\"];");

        Matcher matcher = pattern.matcher(body);

        if (body.toLowerCase().contains(ErrorMessages.NOT_CUSTOMER)) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Couldn't find JS redirect URL. HTML changed?");
        }
    }
}
