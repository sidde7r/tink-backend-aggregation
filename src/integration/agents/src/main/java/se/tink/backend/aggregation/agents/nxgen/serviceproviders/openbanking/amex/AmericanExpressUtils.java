package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

public class AmericanExpressUtils {

    private AmericanExpressUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String createAndGetStorageString(String key) {
        return String.format("transactions?%s=%s", "end_date", key);
    }

    public static String formatAccountId(String accountId) {
        return accountId.substring(accountId.length() - 9);
    }
}
