package se.tink.backend.common.utils.giro;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.tink.backend.core.Giro;
import se.tink.backend.core.SwedishGiroType;

/**
 * Thread safe.
 */
public class GiroParser {

    /**
     * Notation explanation:
     * (^|\s) => Must match start of line or whitespace
     * \\d{N,M} => Must contain at least N and not more than M digits
     * \\d{N} ==> Must contain N digits (?!...) ==> Negative look ahead (not followed by any number)
     */
    private static final Pattern BG_REGEX = Pattern.compile("(^|\\s)\\d{3,4}-\\d{4}(?!\\d)");
    private static final Pattern BG_NO_DASH_REGEX = Pattern.compile("BG (?<!\\d)\\d{7,8}(?!\\d)");
    private static final Pattern PG_REGEX = Pattern.compile("(^|\\s)\\d{3,7}-\\d(?!\\d)");
    private static final Pattern PG_NO_DASH_REGEX = Pattern.compile("PG (?<!\\d)\\d{4,8}(?!\\d)");

    public Giro parse(String description) {

        String uppercaseDescription = description.toUpperCase();

        Giro giro = new Giro();

        Matcher m = BG_REGEX.matcher(uppercaseDescription);

        if (m.find()) {

            giro.setType(SwedishGiroType.BG);
            giro.setAccountNumber(m.group());

            return giro;
        }

        m = BG_NO_DASH_REGEX.matcher(uppercaseDescription);

        if (m.find()) {
            giro.setType(SwedishGiroType.BG);

            String accountNumber = m.group();

            if (accountNumber.length() == 10) {
                accountNumber = accountNumber.substring(3, 6) + "-" + accountNumber.substring(6);
            } else {
                accountNumber = accountNumber.substring(3, 7) + "-" + accountNumber.substring(7);
            }

            giro.setAccountNumber(accountNumber);

            return giro;
        }

        m = PG_REGEX.matcher(uppercaseDescription);

        if (m.find()) {
            giro.setType(SwedishGiroType.PG);
            giro.setAccountNumber(m.group());

            return giro;
        }

        m = PG_NO_DASH_REGEX.matcher(uppercaseDescription);

        if (m.find()) {
            giro.setType(SwedishGiroType.PG);

            String accountNumber = m.group();

            accountNumber = accountNumber.substring(3, accountNumber.length() - 1) + "-" + accountNumber
                    .substring(accountNumber.length() - 1);

            giro.setAccountNumber(accountNumber);

            return giro;
        }

        return null;
    }
}
