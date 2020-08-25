package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressUtils {

    private AmericanExpressUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String createAndGetStorageString(Date fromDate, Date toDate) {
        return String.format(
                "transactions?%s=%s&%s=%s",
                "start_date",
                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate),
                "end_date",
                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));
    }

    public static String formatAccountId(String accountId) {
        return accountId.substring(accountId.length() - 9);
    }

    public static ExactCurrencyAmount createEmptyAmount() {
        return new ExactCurrencyAmount(new BigDecimal(0), "SEK");
    }
}
