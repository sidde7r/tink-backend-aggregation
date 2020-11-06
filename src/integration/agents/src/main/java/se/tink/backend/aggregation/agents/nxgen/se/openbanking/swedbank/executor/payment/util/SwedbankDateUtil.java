package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Payment;

public class SwedbankDateUtil {

    private static final int EXTERNAL_CUTOFF_HOUR = 13;
    private static final int EXTERNAL_CUTOFF_MINUTE = 0;
    private static final int PAYMENT_CUTOFF_HOUR = 10;
    private static final int PAYMENT_CUTOFF_MINUTE = 0;
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    public static Date getExecutionDateOrCurrentDate(Payment payment) {
        switch (payment.getCreditor().getAccountIdentifierType()) {
            case SE_BG:
            case SE_PG:
                return getExecutionDateOrCurrentDateForBGPG(payment);
            case SE:
                return getExecutionDateOrCurrentDateForBankTransfers(payment);
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.INVALID_ACCOUNT_TYPE,
                                payment.getCreditor().getAccountIdentifierType().toString()));
        }
    }

    private static Date getExecutionDateOrCurrentDateForBGPG(Payment payment) {
        Date executionDate = localDateToDate(payment.getExecutionDate());
        return getTransferDateForPayments(executionDate);
    }

    private static Date getTransferDateForInternalTransfer(Date date) {
        return getDateOrNullIfDueDateIsToday(dateHelper.getProvidedDateOrCurrentDate(date));
    }

    private static Date getExecutionDateOrCurrentDateForBankTransfers(Payment payment) {
        Date executionDate = localDateToDate(payment.getExecutionDate());
        return IntraBankChecker.isAccountIdentifierIntraBank(
                        payment.getDebtor().getAccountIdentifier(),
                        payment.getCreditor().getAccountIdentifier())
                ? getTransferDateForInternalTransfer(executionDate)
                : getTransferDateForExternalTransfer(executionDate);
    }

    // Used for testing purposes to be able to set a fixed "now"
    static Date getTransferDateForInternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return getDateOrNullIfDueDateIsToday(dateHelper.getProvidedDateOrCurrentDate(date));
    }

    static Date getTransferDateForExternalTransfer(Date date) {
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    // Used for testing purposes to be able to set a fixed "now"
    static Date getTransferDateForExternalTransfer(Date date, Clock now) {
        dateHelper.setClock(now);
        return getTransferDate(date, EXTERNAL_CUTOFF_HOUR, EXTERNAL_CUTOFF_MINUTE);
    }

    private static Date getTransferDateForPayments(Date date) {
        return getTransferDate(date, PAYMENT_CUTOFF_HOUR, PAYMENT_CUTOFF_MINUTE);
    }

    // Used for testing purposes to be able to set a fixed "now"
    static Date getTransferDateForPayments(Date date, Clock now) {
        dateHelper.setClock(now);
        return getTransferDate(date, PAYMENT_CUTOFF_HOUR, PAYMENT_CUTOFF_MINUTE);
    }

    private static Date getTransferDate(Date date, int cutoffHour, int cutoffMinute) {
        return getDateOrNullIfDueDateIsToday(
                dateHelper.getProvidedDateOrBestPossibleDate(date, cutoffHour, cutoffMinute));
    }

    /**
     * Swedbank reject today's date as a possible execution date. If the payment/transfer is suppose
     * to be executed today the date field needs to be left blank (null).
     *
     * @return Input date if a future date, null if input date is today's date.
     */
    static Date getDateOrNullIfDueDateIsToday(Date transferDate) {
        if (transferDate == null) {
            return null;
        }
        LocalDate todayLocalDate = LocalDate.now(DEFAULT_ZONE_ID);
        LocalDate transferLocalDate =
                transferDate.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDate();
        // Use localdate for comparison as we don't care about time
        if (todayLocalDate.equals(transferLocalDate)) {
            return null;
        }
        return transferDate;
    }

    private static Date localDateToDate(LocalDate localDate) {
        return localDate != null
                ? Date.from(localDate.atStartOfDay().atZone(DEFAULT_ZONE_ID).toInstant())
                : null;
    }
}
