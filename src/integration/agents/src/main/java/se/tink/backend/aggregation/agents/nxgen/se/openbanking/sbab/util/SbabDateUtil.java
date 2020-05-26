package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class SbabDateUtil {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static String getTransferDate(LocalDate date, boolean isInternalTransfer) {
        LocalDate nextPossibleDate =
                isInternalTransfer
                        ? dateHelper.getProvidedDateOrCurrentLocalDate(date)
                        : dateHelper.getProvidedDateOrBestPossibleLocalDate(date, 14, 00);
        return nextPossibleDate.toString();
    }
}
