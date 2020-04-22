package se.tink.backend.aggregation.agents.banks.sbab.util;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SBABDateUtil {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static String getTransferDate(Date date, boolean isInternalTransfer) {
        Date nextPossibleDate =
                isInternalTransfer
                        ? dateHelper.getProvidedDateOrBestPossibleDate(date, 23, 59)
                        : dateHelper.getProvidedDateOrBestPossibleDate(date, 14, 00);
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(nextPossibleDate);
    }
}
