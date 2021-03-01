package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.assertj.core.util.VisibleForTesting;
import se.tink.libraries.date.CountryDateHelper;

public class NordeaDateUtil {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static Date getTransferDateForIntraBankTransfer(Date date) {
        return dateHelper.getProvidedDateOrCurrentDate(date);
    }

    public static Date getTransferDateForInterBankTransfer(Date date) {
        return dateHelper.getProvidedDateOrBestPossibleDate(date, 12, 45);
    }

    public static Date getTransferDateForBgPg(Date date) {
        return dateHelper.getProvidedDateOrBestPossibleDate(date, 9, 45);
    }

    private NordeaDateUtil() {}

    @VisibleForTesting
    static void setClockForTesting(Clock clockForTesting) {
        dateHelper.setClock(clockForTesting);
    }
}
