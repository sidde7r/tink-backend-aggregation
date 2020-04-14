package se.tink.backend.aggregation.agents.banks.seb.utilities;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SEBDateUtil {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static String getTransferDate(Date date, boolean withinSEB) {
        Date nextPossibleDate =
                withinSEB
                        ? dateHelper.getTransferDate(date, 19, 00)
                        : dateHelper.getTransferDate(date, 13, 15);
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(nextPossibleDate);
    }

    public static String getTransferDateForBgPg(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(dateHelper.getTransferDate(date, 9, 45));
    }
}
