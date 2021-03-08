package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class LansforsakringarDateUtil {

    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    private LansforsakringarDateUtil() {}

    public static void setClock(Clock clock) {
        dateHelper.setClock(clock);
    }

    public static LocalDate getCurrentOrNextBusinessDate(LocalDate localDate) {
        return dateHelper.getCurrentOrNextBusinessDay(localDate);
    }
}
