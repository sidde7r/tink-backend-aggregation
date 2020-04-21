package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;

public class SwedbankDateUtils {

    private final CountryDateHelper dateHelper;
    private static final int EXTERNAL_CUTOFF_HOUR = 13;
    private static final int EXTERNAL_CUTOFF_MINUTE = 0;
    private static final int PAYMENT_CUTOFF_HOUR = 10;
    private static final int PAYMENT_CUTOFF_MINUTE = 0;
    private final ZoneId zoneId;

    public SwedbankDateUtils(ZoneId timeZoneId, Locale locale) {
        zoneId = timeZoneId;
        dateHelper = new CountryDateHelper(locale, TimeZone.getTimeZone(timeZoneId));
    }

    public Date getTransferDateForInternalTransfer(Date date) {
        return getDateOrNullIfDueDateIsToday(dateHelper.getProvidedOrTodayDate(date));
    }

    // Used for testing purposes to be able to set a fixed "now"
    Date getTransferDateForInternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return getDateOrNullIfDueDateIsToday(dateHelper.getProvidedOrTodayDate(date));
    }

    public Date getTransferDateForExternalTransfer(Date date) {
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    // Used for testing purposes to be able to set a fixed "now"
    Date getTransferDateForExternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    public Date getTransferDateForPayments(Date date) {
        return getTransferDate(date, PAYMENT_CUTOFF_HOUR, PAYMENT_CUTOFF_MINUTE);
    }

    // Used for testing purposes to be able to set a fixed "now"
    Date getTransferDateForPayments(Date date, Clock now) {
        dateHelper.setClock(now);
        return getTransferDate(date, PAYMENT_CUTOFF_HOUR, PAYMENT_CUTOFF_MINUTE);
    }

    private Date getTransferDate(Date date, int cutoffHour, int cutoffMinute) {
        return getDateOrNullIfDueDateIsToday(
                dateHelper.getTransferDate(date, cutoffHour, cutoffMinute));
    }

    /**
     * Swedbank reject today's date as a possible execution date. If the payment/transfer is suppose
     * to be executed today the date field needs to be left blank (null).
     *
     * @return Input date if a future date, null if input date is today's date.
     */
    Date getDateOrNullIfDueDateIsToday(Date transferDate) {
        if (transferDate == null) {
            return null;
        }
        LocalDate todayLocalDate = LocalDate.now(zoneId);
        LocalDate transferLocalDate = transferDate.toInstant().atZone(zoneId).toLocalDate();
        // Use localdate for comparison as we don't care about time
        if (todayLocalDate.equals(transferLocalDate)) {
            return null;
        }
        return transferDate;
    }
}
