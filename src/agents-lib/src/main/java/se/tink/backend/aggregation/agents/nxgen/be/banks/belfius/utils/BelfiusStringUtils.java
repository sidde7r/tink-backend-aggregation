package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils;

import java.util.Locale;
import java.util.Optional;
import se.tink.libraries.amount.Amount;

public class BelfiusStringUtils {

    public static String formatPanNumber(String panNumber) {
        return panNumber.replaceAll(" ", "").replaceAll(".{4}(?=)", "$0 ");
    }

    public static Optional<Amount> parseStringToAmount(String amount) {
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
        for(String s : decimalSplit){
            formattedAmount = formattedAmount + s;
        }

        return Optional.of(new Amount(currency, Double.valueOf(formattedAmount)));
    }

    /**
     * Formats an amount to a string on the form
     * xxx.xxx,dd
     * Example: 2.312,00
     *
     * @param amount
     * @return a formatted amount
     */
    public static String getFormattedAmount(Amount amount) {
        String form = String.format(Locale.US, "%,.2f", amount.getValue());
        String[] decimal = form.split("\\.");
        decimal[0] = decimal[0].replace(",", ".");
        return decimal[0] + "," + decimal[1];
    }

}
