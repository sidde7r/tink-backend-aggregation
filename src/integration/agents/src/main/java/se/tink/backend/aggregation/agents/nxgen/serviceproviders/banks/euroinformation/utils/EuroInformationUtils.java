package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.amount.Amount;

public class EuroInformationUtils {

    // By default we try to use currency provided in separate field (`currencyInput`)
    // if not available, we parse currency out of amount in `input`.
    // Example `input` format: +1.11SEK
    public static final String AMOUNT_REGEX = "^([-+]?\\d+\\.\\d{2})(|\\w{3})$";

    private static final Pattern pattern = Pattern.compile(AMOUNT_REGEX);

    public static Amount parseAmount(String input) {
        return parseAmountForInput(input, null);
    }

    public static Amount parseAmount(String input, String currencyInput) {
        return parseAmountForInput(input, currencyInput);
    }

    private static Amount parseAmountForInput(String input, String currencyInput) {
        Optional<String> currencyOptional = Optional.ofNullable(currencyInput);

        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse amount: " + input);
        }
        Double amountInDouble = Double.parseDouble(matcher.group(1));
        return new Amount(currencyOptional.orElse(matcher.group(2)), amountInDouble);
    }

    public static boolean isSuccess(String responseReturnCode) {
        return EuroInformationErrorCodes.SUCCESS.equals(
                EuroInformationErrorCodes.getByCodeNumber(responseReturnCode));
    }
}
