package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class FrOpenBankingDateUtil {

    private FrOpenBankingDateUtil() {}

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");
    private static final Locale DEFAULT_LOCALE = new Locale("fr_FR", "FR");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static LocalDate getExecutionDate(LocalDate localDate) {
        return dateHelper.getProvidedDateOrBestPossibleLocalDate(localDate, 23, 59);
    }

    public static LocalDateTime getCreationDate() {
        return LocalDateTime.now(DEFAULT_ZONE_ID);
    }

    public static boolean isBusinessDate(LocalDate localDate) {
        return dateHelper.isBusinessDay(
                Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
    }
}
