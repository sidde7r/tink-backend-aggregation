package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;

public class SwedbankDateUtilTest {

    // Cutoff is 23:59:59
    private final LocalDateTime INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 1, 12, 20);
    private final LocalDateTime INTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 2, 12, 20);
    // Cutoff is 13:00
    private final LocalDateTime EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 1, 14, 0);
    private final LocalDateTime EXTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 1, 12, 0);
    private final LocalDateTime EXTERNAL_AFTER_CUTOFF_TIME_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 2, 14, 0);
    private final LocalDateTime EXTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 2, 12, 0);
    // Cutoff is 10:00
    private final LocalDateTime PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 1, 13, 0);
    private final LocalDateTime PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 1, 9, 0);
    private final LocalDateTime PAYMENT_AFTER_CUTOFF_TIME_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 2, 13, 0);
    private final LocalDateTime PAYMENT_BEFORE_CUTOFF_TIME_BUSINESS_DAY =
            LocalDateTime.of(2020, 1, 2, 9, 0);

    private final ZoneId zoneId = ZoneId.of("Europe/Stockholm");
    private final Locale locale = new Locale("sv", "SE");
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId), locale);

    @Test
    public void testInternalTransferDateBeforeCutoffTime() {
        LocalDate dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForInternalTransfer(
                                convertToDateViaInstant(
                                        INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY),
                                getClock(INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForInternalTransfer(
                                null, getClock(INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(INTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForInternalTransfer(
                                null, getClock(INTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY)));
        assertEquals(INTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY.toLocalDate(), dueDate);
    }

    @Test
    public void testExternalTransferDateBeforeCutoffTime() {
        LocalDate dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                convertToDateViaInstant(
                                        EXTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(EXTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                null, getClock(EXTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(
                EXTERNAL_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                null, getClock(EXTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY)));
        assertEquals(EXTERNAL_BEFORE_CUTOFF_TIME_BUSINESS_DAY.toLocalDate(), dueDate);
    }

    @Test
    public void testExternalTransferDateAfterCutoffTime() {
        LocalDate dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                convertToDateViaInstant(
                                        EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY),
                                getClock(EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                null, getClock(EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(
                EXTERNAL_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForExternalTransfer(
                                null, getClock(EXTERNAL_AFTER_CUTOFF_TIME_BUSINESS_DAY)));
        assertEquals(EXTERNAL_AFTER_CUTOFF_TIME_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);
    }

    @Test
    public void testPaymentTransferDateBeforeCutoffTime() {
        LocalDate dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                convertToDateViaInstant(
                                        PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY),
                                getClock(PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                null, getClock(PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(
                PAYMENT_BEFORE_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                null, getClock(PAYMENT_BEFORE_CUTOFF_TIME_BUSINESS_DAY)));
        assertEquals(PAYMENT_BEFORE_CUTOFF_TIME_BUSINESS_DAY.toLocalDate(), dueDate);
    }

    @Test
    public void testPaymentTransferDateAfterCutoffTime() {
        LocalDate dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                convertToDateViaInstant(PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY),
                                getClock(PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate(), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                null, getClock(PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY)));
        assertEquals(PAYMENT_AFTER_CUTOFF_TIME_NON_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);

        dueDate =
                convertToLocalDateViaMilisecond(
                        SwedbankDateUtil.getTransferDateForPayments(
                                null, getClock(PAYMENT_AFTER_CUTOFF_TIME_BUSINESS_DAY)));
        assertEquals(PAYMENT_AFTER_CUTOFF_TIME_BUSINESS_DAY.toLocalDate().plusDays(1), dueDate);
    }

    private Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Clock getClock(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(zoneId).toInstant(), zoneId);
    }

    @Test
    public void verifyGetDateOrNullIfDueDateIsToday_ReturnsNull_IfInputDateIsToday() {
        Date currentDate = calendar.getTime();
        Date dateToSendToBank = SwedbankDateUtil.getDateOrNullIfDueDateIsToday(currentDate);
        assertNull(dateToSendToBank);
    }

    @Test
    public void verifyGetDateOrNullIfDueDateIsToday_ReturnsInputDate_IfInputDateIsFuture() {
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date futureDate = calendar.getTime();
        Date dateToSendToBank = SwedbankDateUtil.getDateOrNullIfDueDateIsToday(futureDate);
        assertEquals(dateToSendToBank, futureDate);
    }

    @Test
    public void verifyGetDateOrNullIfDueDateIsToday_ReturnsNull_IfInputDateIsNull() {
        Date dateToSendToBank = SwedbankDateUtil.getDateOrNullIfDueDateIsToday(null);
        assertNull(dateToSendToBank);
    }
}
