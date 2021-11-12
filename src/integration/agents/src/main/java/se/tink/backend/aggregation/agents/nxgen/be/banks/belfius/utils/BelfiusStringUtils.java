package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils;

import java.util.Optional;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BelfiusStringUtils {

    public static String formatPanNumber(String panNumber) {
        return panNumber.replaceAll(" ", "").replaceAll(".{4}(?=)", "$0 ");
    }

    public static Optional<ExactCurrencyAmount> parseStringToAmount(String amount) {
        if (amount == null || !amount.contains(" ")) {
            return Optional.empty();
        }

        String[] amountCurrencySplit = amount.split(" ");
        String unformattedAmount = amountCurrencySplit[0];
        String currency = amountCurrencySplit[1];

        if (!currency.equalsIgnoreCase("EUR")) {
            return Optional.empty();
        }

        String[] decimalSplit = unformattedAmount.split("\\.");
        decimalSplit[decimalSplit.length - 1] =
                decimalSplit[decimalSplit.length - 1].replace(",", ".");

        String formattedAmount = "";
        for (String s : decimalSplit) {
            formattedAmount = formattedAmount + s;
        }

        return Optional.of(ExactCurrencyAmount.of(Double.valueOf(formattedAmount), currency));
    }
}
