package se.tink.backend.aggregation.agents.banks.danskebank.v2.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class DanskeBankDateUtil {

    private final ZoneId zoneId;
    private final CountryDateHelper dateHelper;
    private static final int EXTERNAL_CUTOFF_HOUR = 13;
    private static final int EXTERNAL_CUTOFF_MINUTE = 0;
    private static final int BGPG_CUTOFF_HOUR = 10;
    private static final int BGPG_CUTOFF_MINUTE = 0;
    private static final String TODAY_AS_TRANSFER_DATE = "Now";

    public DanskeBankDateUtil(ZoneId timeZoneId, Locale locale) {
        zoneId = timeZoneId;
        dateHelper = new CountryDateHelper(locale, TimeZone.getTimeZone(timeZoneId));
    }

    public String getTransferDateForInternalTransfer(Date date) {
        return getTransferDate(date);
    }

    public String getTransferDateForInternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return getTransferDate(date);
    }

    public String getTransferDateForExternalTransfer(Date date) {
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    public String getTransferDateForExternalTransfer(Date date, Clock now) {
        // according to previous logic, Danskebanken does not accepts today's date for external
        // transfers
        // but according to new info, it accepts same day's transfers, if they are done before 13:00
        dateHelper.setClock(now);
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    public String getTransferDateForBgPg(Date date) {
        return getTransferDate(date, BGPG_CUTOFF_HOUR, BGPG_CUTOFF_MINUTE);
    }

    public String getTransferDateForBgPg(Date date, Clock now) {
        // according to previous logic, Danskebanken does not accepts today's date for bg/pg
        // payments
        // but according to new info, it accepts same day's transfers, if they are done before 10:00
        dateHelper.setClock(now);
        return getTransferDate(date, BGPG_CUTOFF_HOUR, BGPG_CUTOFF_MINUTE);
    }

    private String getTransferDate(Date date, int cutoffHour, int cutoffMinute) {
        LocalDate localDate =
                dateHelper
                        .getProvidedDateOrBestPossibleDate(date, cutoffHour, cutoffMinute)
                        .toInstant()
                        .atZone(zoneId)
                        .toLocalDate();
        return localDate.isEqual(LocalDate.now(dateHelper.getClock()))
                ? TODAY_AS_TRANSFER_DATE
                : localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String getTransferDate(Date date) {
        LocalDate localDate =
                dateHelper
                        .getProvidedDateOrCurrentDate(date)
                        .toInstant()
                        .atZone(zoneId)
                        .toLocalDate();
        return localDate.isEqual(LocalDate.now(dateHelper.getClock()))
                ? TODAY_AS_TRANSFER_DATE
                : localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
