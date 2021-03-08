package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SebDateUtil {

    private SebDateUtil() {}

    private static final int EXTERNAL_CUTOFF_HOUR = 13;
    private static final int EXTERNAL_CUTOFF_MINUTE = 35;
    private static final int BGPG_CUTOFF_HOUR = 9;
    private static final int BGPG_CUTOFF_MINUTE = 45;
    private static final int SEPA_CUTOFF_HOUR = 14;
    private static final int SEPA_CUTOFF_MINUTE = 0;
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static String getTransferDate(LocalDate localDate, boolean withinSeb) {

        Date nextPossibleDate =
                withinSeb
                        ? dateHelper.getProvidedDateOrCurrentDate(localDateToDate(localDate))
                        : dateHelper.getProvidedDateOrBestPossibleDate(
                                localDateToDate(localDate),
                                EXTERNAL_CUTOFF_HOUR,
                                EXTERNAL_CUTOFF_MINUTE);
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(nextPossibleDate);
    }

    public static String getTransferDateForBgPg(LocalDate localDate) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                dateHelper.getProvidedDateOrBestPossibleDate(
                        localDateToDate(localDate), BGPG_CUTOFF_HOUR, BGPG_CUTOFF_MINUTE));
    }

    public static String getTransferDateForSepa(LocalDate localDate) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                dateHelper.getProvidedDateOrBestPossibleDate(
                        localDateToDate(localDate), SEPA_CUTOFF_HOUR, SEPA_CUTOFF_MINUTE));
    }

    private static Date localDateToDate(LocalDate localDate) {
        return localDate != null
                ? Date.from(localDate.atStartOfDay().atZone(DEFAULT_ZONE_ID).toInstant())
                : null;
    }
}
