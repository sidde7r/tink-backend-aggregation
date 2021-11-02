package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Payment;

public class SocieteGeneraleDateUtil {

    private SocieteGeneraleDateUtil() {}

    private static final int CUTOFF_HOUR = 17;
    private static final int CUTOFF_MINUTE = 14;
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");
    private static final Locale DEFAULT_LOCALE = new Locale("fr_FR", "FR");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static String getExecutionDate(Payment payment) {
        LocalDate nextPossibleDate = provideNextPossibleDate(payment);

        return ZonedDateTime.of(
                        nextPossibleDate,
                        LocalTime.now(DEFAULT_ZONE_ID).plusMinutes(1),
                        DEFAULT_ZONE_ID)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static LocalDate provideNextPossibleDate(Payment payment) {
        LocalDate executionDate = payment.getExecutionDate();
        if (payment.isSepaInstant()) {
            return executionDate;
        } else {
            return dateHelper.getProvidedDateOrBestPossibleLocalDate(
                    executionDate, CUTOFF_HOUR, CUTOFF_MINUTE);
        }
    }

    public static String getCreationDate() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
