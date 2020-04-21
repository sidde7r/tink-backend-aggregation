package se.tink.backend.aggregation.agents.banks.danskebank.v2.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DanskeBankDateUtil {

    private final ZoneId zoneId;
    private final CountryDateHelper dateHelper;

    public DanskeBankDateUtil(ZoneId timeZoneId, Locale locale) {
        zoneId = timeZoneId;
        dateHelper = new CountryDateHelper(locale, TimeZone.getTimeZone(timeZoneId));
    }

    public String getTransferDateForInternalTransfer(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                getTransferDateForInternalTransfer(
                        date, Clock.fixed(LocalDateTime.now().atZone(zoneId).toInstant(), zoneId)));
    }

    public Date getTransferDateForInternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return dateHelper.getProvidedOrTodayDate(date);
    }

    public String getTransferDateForExternalTransfer(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                getTransferDateForExternalTransfer(
                        date, Clock.fixed(LocalDateTime.now().atZone(zoneId).toInstant(), zoneId)));
    }

    public Date getTransferDateForExternalTransfer(Date date, Clock now) {
        // according to previous logic, Danskebanken does not accepts today's date for external
        // transfers
        // but according to new info, it accepts same day's transfers, if they are done before 13:00
        dateHelper.setClock(now);
        return dateHelper.getTransferDate(date, 13, 0);
    }

    public String getTransferDateForBgPg(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                getTransferDateForBgPg(
                        date, Clock.fixed(LocalDateTime.now().atZone(zoneId).toInstant(), zoneId)));
    }

    public Date getTransferDateForBgPg(Date date, Clock now) {
        // according to previous logic, Danskebanken does not accepts today's date for bg/pg
        // payments
        // but according to new info, it accepts same day's transfers, if they are done before 10:00
        dateHelper.setClock(now);
        return dateHelper.getTransferDate(date, 10, 0);
    }
}
