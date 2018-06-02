package se.tink.backend.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Currency;

public class StringUtils extends se.tink.libraries.strings.StringUtils {
    /**
     * Helper function to generate a UUID without dashes.
     * <p>
     * TODO: Migrate to UUIDUtils. Feels more natural there.
     *
     * @return
     */
    public static String generateUUID() {
        return UUIDUtils.toTinkUUID(UUID.randomUUID());
    }

    public static String formatCurrency(double amount, int decimals, Currency currency, Locale locale) {
        String currencyFormatted = getDecimalFormat(locale, decimals).format(amount);

        if (currency.isPrefixed()) {
            return (currency.getSymbol() + currencyFormatted);
        } else {
            return (currencyFormatted + NON_BREAKING_WHITESPACE + currency.getSymbol());
        }
    }

    private static DecimalFormat getDecimalFormat(Locale locale, int decimals) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);

        DecimalFormat df = (DecimalFormat) nf;

        StringBuilder sb = new StringBuilder();
        sb.append("###,###");
        if (decimals > 0) {
            sb.append(".");
        }

        for (int i = 0; i < decimals; i++) {
            sb.append("#");
        }

        df.applyPattern(sb.toString());

        return df;
    }
}
