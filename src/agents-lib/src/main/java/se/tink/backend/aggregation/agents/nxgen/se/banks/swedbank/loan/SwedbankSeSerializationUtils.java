package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

public class SwedbankSeSerializationUtils {
    // In logs we see that the currencyCode is as separate field, but as we cannot be sure if
    // it's required field we add default SEK for sweden
    public static final String AMOUNT_REGEX = "^([+-]?)([0-9].+)";
    public static final String INTEREST_RATE_REGEX = "^([0-9].+)\\%$";
    public static final String SEK = "SEK";
    private static final Pattern amountPattern = Pattern.compile(AMOUNT_REGEX);
    private static final Pattern interestRatePttern = Pattern.compile(INTEREST_RATE_REGEX);

    public static Amount parseAmountForInput(String input, String currencyInput) {
        Optional<String> currencyOptional = Optional.ofNullable(currencyInput);

        Matcher matcher = amountPattern.matcher(input);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse amount: " + input);

        }
        int sign = "-".equals(matcher.group(1)) ? -1 : 1;
        Double amountInDouble = StringUtils.parseAmount(matcher.group(2)) * sign;
        return new Amount(currencyOptional.orElse(SEK), amountInDouble);
    }

    public static Double parseInterestRate(String interest) {
        Matcher matcher = interestRatePttern.matcher(interest);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse interest rate: " + interest);
        }
        Double interestRate = StringUtils.parseAmount(matcher.group(1)) / 100d;
        return interestRate;
    }

    /**
     * Converts a time string into a number of months, e.g. 3 mån -> 3 and 5 år -> 60.
     */
    public static int parseNumMonthsBound(String fixedInterestPeriod) {
        if (fixedInterestPeriod != null && fixedInterestPeriod.indexOf(' ') != -1) {

            String[] parts = fixedInterestPeriod.split(" ");
            String quantity = parts[0];
            String unit = parts[1];

            if (parts.length == 2) {
                if (SwedbankSEConstants.LOAN_YEARS.equalsIgnoreCase(unit)) {
                    return Integer.parseInt(quantity) * 12;
                } else if (SwedbankSEConstants.LOAN_MONTHS.equalsIgnoreCase(unit)) {
                    return Integer.parseInt(quantity);
                }
            }
        }

        return 0;
    }
}
