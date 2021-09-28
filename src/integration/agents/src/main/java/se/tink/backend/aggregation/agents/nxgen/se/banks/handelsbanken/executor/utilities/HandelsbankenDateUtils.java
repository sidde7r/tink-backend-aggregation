package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.assertj.core.util.VisibleForTesting;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenDateUtils {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public String getTransferDateForInternalTransfer(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                dateHelper.getProvidedDateOrBestPossibleDate(date, 23, 59));
    }

    public String getTransferDateForExternalTransfer(Date date) {

        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                dateHelper.getProvidedDateOrBestPossibleDate(date, 14, 00));
    }

    public String getTransferDateForBgPg(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                dateHelper.getProvidedDateOrBestPossibleDate(date, 00, 00));
    }

    @VisibleForTesting
    static void setClockForTesting(Clock clockForTesting) {
        dateHelper.setClock(clockForTesting);
    }
}
