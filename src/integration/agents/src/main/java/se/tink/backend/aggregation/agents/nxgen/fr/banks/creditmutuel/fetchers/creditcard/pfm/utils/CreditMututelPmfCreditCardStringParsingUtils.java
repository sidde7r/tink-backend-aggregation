package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

public class CreditMututelPmfCreditCardStringParsingUtils {
    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final String WINDOWS_NEW_LINE = "\r\n";
    private static String MASKED_NUMBERS = "XXXX ";
    // Input example
    // Retraits max 300,00 EUR
    private static Pattern MAX_AMOUNT_PATTERN = Pattern.compile("(\\d+[,.]\\d{2})\\s(\\w+)");

    // Input example:
    // Name Surname \r\n 123X XXXX XXXX 5678 - 01/2010\r\n Active
    public static String parseCreditCardNumber(String creditCardNumberString) {
        return Optional.ofNullable(creditCardNumberString).map(s -> {
            String onlyCardNumber = extractOnlyCardNumber(s);
            String lastFourDigits = getLastFourDigits(onlyCardNumber);

            return (new StringBuilder())
                    .append(MASKED_NUMBERS)
                    .append(MASKED_NUMBERS)
                    .append(MASKED_NUMBERS)
                    .append(lastFourDigits)
                    .toString();
        })
                .orElseThrow(IllegalStateException::new);
    }

    private static String extractOnlyCardNumber(String wholeInput) {
        String[] splitByNewLines = wholeInput.split(WINDOWS_NEW_LINE);
        String numberWithoutDate = splitByNewLines[1].split(DASH)[0].trim();
        return numberWithoutDate;
    }

    private static String getLastFourDigits(String notMaskedNumber) {
        String trimedNotMaskedNumber = notMaskedNumber.trim();
        String[] splittedNumbers = trimedNotMaskedNumber.split(SPACE);
        int length = splittedNumbers.length;
        String lastFourDigits = splittedNumbers[length - 1];
        return lastFourDigits;
    }

    public static Amount extractAmountFromString(String input) {
        Matcher matcher = MAX_AMOUNT_PATTERN.matcher(input);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse amount: " + input);
        }
        String amount = matcher.group(1);
        String currency = matcher.group(2);
        return new Amount(currency, StringUtils.parseAmount(amount));
    }

}
