package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils;

public class SHBUtils {
    public static String unescapeAndCleanTransactionDescription(String input) {
        String unescapedDescription = unescapeTransactionDescription(input);
        return cleanDescription(unescapedDescription);
    }

    private static String unescapeTransactionDescription(String input) {
        // This is not unescapable by StringEscapeUtils for some reason...

        return input.replace("&APOS;", "'").replace("&apos;", "'");
    }

    private static String cleanDescription(String input) {
        // Sometimes the description of transactions in Handelsbanken has strange signs instead of
        // letters.
        // It seems as each sign always represent a certain letter. We have found the ones bellow.

        return input.replace("$", "Å").replace("{", "Ä").replace("@", "Ö");
    }
}
