package se.tink.libraries.date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.function.Consumer;
import org.junit.Test;

public class BelgianDateUtilsTest {
    private static void testDayBoundaries(Consumer<Calendar> consumer) {
        Calendar c = DateUtils.getCalendar();
        c.set(Calendar.YEAR, 2018);

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        consumer.accept(c);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        consumer.accept(c);
    }

    @Test
    public void independenceDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 5);
                    c.set(Calendar.DATE, 6);
                    assertTrue(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void boxingDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 26);
                    assertTrue(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void christmasDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 25);
                    assertFalse(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void christmasEveTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 24);
                    assertTrue(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void saintsDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 10);
                    c.set(Calendar.DATE, 1);
                    assertFalse(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void kingsDay() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 3);
                    c.set(Calendar.DATE, 2);
                    assertFalse(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void mayDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 4);
                    c.set(Calendar.DATE, 7);
                    assertTrue(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }

    @Test
    public void labourDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 4);
                    c.set(Calendar.DATE, 1);
                    assertFalse(c.toString(), BelgianDateUtils.isBusinessDay(c));
                });
    }
}
