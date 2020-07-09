package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities;

import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenDateUtils {

    private final CountryDateHelper dateHelper;

    public HandelsbankenDateUtils() {
        ZoneId defaultZoneId = ZoneId.of("CET");
        Locale defaultLocale = new Locale("sv", "SE");
        dateHelper = new CountryDateHelper(defaultLocale, TimeZone.getTimeZone(defaultZoneId));
    }

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
}
