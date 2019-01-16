package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import se.tink.backend.core.Amount;

public class IngAtAmmountParser {

    /** Converts e.g. "€ 1.234,56" -> 1234.56 */
    public static Amount toAmount(final String amountString) {
        double amount =
                Double.parseDouble(
                        amountString
                                .replaceAll("\\s+", "")
                                .replace("€", "")
                                .replace(".", "")
                                .replace(",", "."));
        return new Amount("EUR", amount);
    }
}
