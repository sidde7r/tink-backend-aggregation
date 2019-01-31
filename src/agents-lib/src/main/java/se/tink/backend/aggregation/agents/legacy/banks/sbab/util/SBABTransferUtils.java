package se.tink.backend.aggregation.agents.banks.sbab.util;

import se.tink.libraries.transfer.rpc.Transfer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SBABTransferUtils {

    /**
     * This method makes sure that we always send transfer values to SBAB in the Swedish format, with a comma as
     * separator. Also, SBAB seem to use two decimal places, so this method also forces that formatting of the amount.
     */
    public static String formatAmount(Transfer transfer) {
        Locale swedishLocale = new Locale("sv", "SE");
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(swedishLocale);
        DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        return decimalFormat.format(transfer.getAmount().getValue());
    }

    public static String formatNegativeAmount(Transfer transfer) {
        return "-" + formatAmount(transfer);
    }
}
