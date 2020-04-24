package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngAtAmountParser {

    private IngAtAmountParser() {}

    /** Converts e.g. "€ 1.234,56" -> 1234.56 */
    public static ExactCurrencyAmount toAmount(final String amountString) {
        double amount =
                Double.parseDouble(
                        amountString
                                .replaceAll("\\s+", "")
                                .replace("€", "")
                                .replace(".", "")
                                .replace(",", "."));
        return ExactCurrencyAmount.inEUR(amount);
    }
}
