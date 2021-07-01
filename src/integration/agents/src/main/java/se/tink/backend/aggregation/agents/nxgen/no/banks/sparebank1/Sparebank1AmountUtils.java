package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Strings;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

public class Sparebank1AmountUtils {

    public static ExactCurrencyAmount constructAmount(String amountInteger, String amountFraction) {
        return ExactCurrencyAmount.inNOK(constructDouble(amountInteger, amountFraction));
    }

    public static double constructDouble(String amountInteger, String amountFraction) {
        return StringUtils.parseAmount(
                emptyToZero(amountInteger) + "," + emptyToZero(amountFraction));
    }

    private static String emptyToZero(String s) {
        return Strings.isNullOrEmpty(s) ? "0" : s;
    }
}
