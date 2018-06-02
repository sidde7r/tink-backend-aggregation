package se.tink.backend.aggregation.agents.banks.swedbank.model;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.util.Date;
import java.util.Locale;

public class PaymentRequestTest {
    private static final Catalog CATALOG = new Catalog(new Locale("sv"));

    @Test
    public void ensureDueDate_isNull_whenDueDateIsTodayOrNextBusinessDay() {
        Date dueDate = DateUtils.getCurrentOrNextBusinessDay();

        ensureDueDate_isNullFor(dueDate);
    }

    @Test
    public void ensureDueDate_isNull_whenDueDateIsTomorrowOrNextBusinessDay() {
        Date dueDate = DateUtils.getNextBusinessDay();

        ensureDueDate_isNullFor(dueDate);
    }

    @Test
    public void ensureDueDate_isNull_whenDueDateIsToday() {
        Date dueDate = DateUtils.inclusiveEndTime(new Date());

        ensureDueDate_isNullFor(dueDate);
    }

    @Test
    public void ensureDueDate_isNull_whenDueDateIsTomorrow() {
        Date dueDate = DateUtils.daysFromNow(1);

        ensureDueDate_isNullFor(dueDate);
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureExceptionIsThrown_whenDueDateIsMoreThan_oneYearInTheFuture() {
        Date dueDate = DateUtils.daysFromNow(366);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setFormattedDueDate(dueDate, CATALOG);
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureExceptionIsThrown_whenDueDateIsEqualTo_oneYearInTheFuture() {
        Date dueDate = DateUtils.daysFromNow(365);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setFormattedDueDate(dueDate, CATALOG);
    }

    @Test(expected = TransferExecutionException.class)
    public void ensureExceptionIsThrown_whenDueDateIsEqualTo_currentOrPreviousBusinessDayOneYearInTheFuture() {
        Date dueDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.daysFromNow(365));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setFormattedDueDate(dueDate, CATALOG);
    }

    @Test
    public void ensureDueDateIsSet_whenDateWithin_twoBusinessDaysFrom_andCurrentOrPreviousBusinessDayOneYearFromNow() {
        // Subtract 10 days from a year to ensure holidays can't fail the test
        Date dueDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.daysFromNow(355));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setFormattedDueDate(dueDate, CATALOG);

        Assert.assertEquals(paymentRequest.getDate(), ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate));
    }

    private void ensureDueDate_isNullFor(Date dueDate) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setFormattedDueDate(dueDate, CATALOG);

        Assert.assertTrue(paymentRequest.getDate() == null);
    }
}
