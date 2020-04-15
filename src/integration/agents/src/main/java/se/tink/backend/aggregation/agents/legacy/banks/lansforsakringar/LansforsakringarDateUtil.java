package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class LansforsakringarDateUtil {

    private LansforsakringarDateUtil() {}

    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static Long getNextPossiblePaymentDateForBgPg(Date dateFromTransfer) {
        return dateHelper.getTransferDate(dateFromTransfer, 10, 0).getTime();
    }
}
