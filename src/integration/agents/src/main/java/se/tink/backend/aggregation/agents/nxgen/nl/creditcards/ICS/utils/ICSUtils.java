package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ICSUtils {

    public static String getInteractionId() {
        return UUID.randomUUID().toString();
    }

    // Not required to send actual values, sending dummy data
    public static String getFinancialId() {
        return "e3213dfd-435fgrd5-e7edr4";
    }

    // Can fetch transactions max 3 years back
    public static Date getFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -3);
        return c.getTime();
    }

    public static Date getToAndExpiredDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 89);
        return c.getTime();
    }

    public static String getLastLoggedTime(Date date) {
        final SimpleDateFormat lastLoggedTimeFormat =
                new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z");
        return lastLoggedTimeFormat.format(date);
    }

    /* Earliest transaction date set in consent. This will not exist for users who logged in before
    it was implemented; a max value is used and the error is handled in ICSCreditCardFetcher */
    public static Date getConsentTransactionDate(PersistentStorage persistentStorage) {
        return persistentStorage
                .get(StorageKeys.TRANSACTION_FROM_DATE, Date.class)
                .orElseGet(ICSUtils::getFallbackFromDate);
    }

    /* Fallback from date used when user doesn't have a date in persistent storage.
    Maximum is 2 years and 89 days, since previously consents were created for 2 years. */
    private static Date getFallbackFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -2);
        c.add(Calendar.DATE, -89);
        return c.getTime();
    }
}
