package se.tink.libraries.date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.junit.Test;

public abstract class CountryDateHelperTest {
    private final CountryDateHelper belgianHelper = new CountryDateHelper(new Locale("nl", "BE"));
    private final CountryDateHelper swedishHelper = new CountryDateHelper();
    private final CountryDateHelper unitedKingdomHelper =
            new CountryDateHelper(new Locale("en", "GB"), TimeZone.getTimeZone("GMT"));

    private void testDayBoundaries(Consumer<Calendar> consumer) {
        Calendar c = swedishHelper.getCalendar();
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
    public void swedishIndependenceDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 5);
                    c.set(Calendar.DATE, 6);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertTrue(c.toString(), belgianHelper.isBusinessDay(c));
                    assertTrue(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void boxingDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 26);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertTrue(c.toString(), belgianHelper.isBusinessDay(c));
                    assertFalse(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void christmasDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 25);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertFalse(c.toString(), belgianHelper.isBusinessDay(c));
                    assertFalse(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void christmasEveTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 24);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertTrue(c.toString(), belgianHelper.isBusinessDay(c));
                    assertTrue(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void saintsDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 10);
                    c.set(Calendar.DATE, 1);
                    assertTrue(c.toString(), swedishHelper.isBusinessDay(c));
                    assertFalse(c.toString(), belgianHelper.isBusinessDay(c));
                    assertTrue(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void kingsDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 3);
                    c.set(Calendar.DATE, 2);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertFalse(c.toString(), belgianHelper.isBusinessDay(c));
                    assertTrue(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void mayDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 4);
                    c.set(Calendar.DATE, 7);
                    assertTrue(c.toString(), swedishHelper.isBusinessDay(c));
                    assertTrue(c.toString(), belgianHelper.isBusinessDay(c));
                    assertFalse(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void labourDayTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 4);
                    c.set(Calendar.DATE, 1);
                    assertFalse(c.toString(), swedishHelper.isBusinessDay(c));
                    assertFalse(c.toString(), belgianHelper.isBusinessDay(c));
                    assertTrue(c.toString(), unitedKingdomHelper.isBusinessDay(c));
                });
    }

    @Test
    public void isTomorrowBeforeTodayTest() {
        Date tomorrow = DateUtils.addDays(swedishHelper.getToday(), 1);
        assertFalse(swedishHelper.isBeforeToday(tomorrow));

        tomorrow = DateUtils.addDays(belgianHelper.getToday(), 1);
        assertFalse(belgianHelper.isBeforeToday(tomorrow));

        tomorrow = DateUtils.addDays(unitedKingdomHelper.getToday(), 1);
        assertFalse(unitedKingdomHelper.isBeforeToday(tomorrow));
    }

    @Test
    public void isTodayBeforeTodayTest() {
        Date today = swedishHelper.getToday();
        assertFalse(swedishHelper.isBeforeToday(today));

        today = belgianHelper.getToday();
        assertFalse(belgianHelper.isBeforeToday(today));

        today = unitedKingdomHelper.getToday();
        assertFalse(unitedKingdomHelper.isBeforeToday(today));
    }

    @Test
    public void isToday1amBeforeTodayTest() {
        Calendar today = swedishHelper.getCalendar(swedishHelper.getToday());
        today.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(swedishHelper.isBeforeToday(today.getTime()));

        today = belgianHelper.getCalendar(belgianHelper.getToday());
        today.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(belgianHelper.isBeforeToday(today.getTime()));

        today = unitedKingdomHelper.getCalendar(unitedKingdomHelper.getToday());
        today.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(unitedKingdomHelper.isBeforeToday(today.getTime()));
    }

    @Test
    public void isYesterdayBeforeTodayTest() {
        Date yesterday = DateUtils.addDays(swedishHelper.getToday(), -1);
        assertTrue(swedishHelper.isBeforeToday(yesterday));

        yesterday = DateUtils.addDays(belgianHelper.getToday(), -1);
        assertTrue(belgianHelper.isBeforeToday(yesterday));

        yesterday = DateUtils.addDays(unitedKingdomHelper.getToday(), -1);
        assertTrue(unitedKingdomHelper.isBeforeToday(yesterday));
    }

    @Test
    public void isYesterday11pmBeforeTodayTest() {
        Calendar yesterday =
                swedishHelper.getCalendar(DateUtils.addDays(swedishHelper.getToday(), -1));
        yesterday.set(Calendar.HOUR_OF_DAY, 23);
        assertTrue(swedishHelper.isBeforeToday(yesterday.getTime()));

        yesterday = belgianHelper.getCalendar(DateUtils.addDays(belgianHelper.getToday(), -1));
        yesterday.set(Calendar.HOUR_OF_DAY, 23);
        assertTrue(belgianHelper.isBeforeToday(yesterday.getTime()));

        yesterday =
                unitedKingdomHelper.getCalendar(
                        DateUtils.addDays(unitedKingdomHelper.getToday(), -1));
        yesterday.set(Calendar.HOUR_OF_DAY, 23);
        assertTrue(unitedKingdomHelper.isBeforeToday(yesterday.getTime()));
    }
}
