package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.parsers;

import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;

public class NumMonthBoundParser {

    private NumMonthBoundParser() {
    }

    public static Integer parse(String value) {
        if (HandelsbankenSEConstants.Fetcher.Loans.FLOATING.equals(value)) {
            return HandelsbankenSEConstants.Fetcher.Loans.FLOATING_REEVALUATION_PERIOD;
        }
        if (value == null) {
            throw new IllegalArgumentException(HandelsbankenSEConstants.Fetcher.Loans.LOG_TAG +
                    " - Cannot parse numbers of months bound from null value.");
        }
        Matcher matcher = HandelsbankenSEConstants.Fetcher.Loans.PERIOD_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(HandelsbankenSEConstants.Fetcher.Loans.LOG_TAG +
                    " - Cannot parse numbers of months bound from value: " + value);
        }
        return parse(matcher);
    }

    private static Integer parse(Matcher matcher) {
        int length = Integer.parseInt(matcher.group(HandelsbankenSEConstants.Fetcher.Loans.LENGTH));
        if (isYear(matcher.group(HandelsbankenSEConstants.Fetcher.Loans.PERIOD))) {
            return length * 12;
        }
        return length;
    }

    private static boolean isYear(String word) {
        return HandelsbankenSEConstants.Fetcher.Loans.YEAR.equalsIgnoreCase(word);
    }
}
