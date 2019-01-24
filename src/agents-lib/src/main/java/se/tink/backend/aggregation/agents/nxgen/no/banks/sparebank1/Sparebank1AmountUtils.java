package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import se.tink.backend.core.Amount;
import se.tink.libraries.strings.StringUtils;

public class Sparebank1AmountUtils {

    public static Amount constructAmount(String amountInteger, String amountFraction) {
        return Amount.inNOK(constructDouble(amountInteger, amountFraction));
    }

    public static double constructDouble(String amountInteger, String amountFraction) {
        return StringUtils.parseAmount(amountInteger + "," + amountFraction);
    }
}
