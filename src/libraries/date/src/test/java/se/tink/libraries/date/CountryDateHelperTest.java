package se.tink.libraries.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Test;

public class CountryDateHelperTest {
    private final CountryDateHelper belgianHelper = new CountryDateHelper(new Locale("nl", "BE"));
    private final CountryDateHelper frenchHelper = new CountryDateHelper(Locale.FRANCE);
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

    private Clock fixedClock(String moment) {
        Instant instant = Instant.parse(moment);
        return Clock.fixed(instant, ZoneId.of("CET"));
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
    public void newYearsEveTest() {
        testDayBoundaries(
                c -> {
                    c.set(Calendar.MONTH, 11);
                    c.set(Calendar.DATE, 31);
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

    @Test
    public void getCurrentOrNextBusinessDayReturnsCurrentDate() {
        LocalDate localDate = LocalDate.of(2021, 2, 1);
        LocalDate currentOrNextBusinessDay = frenchHelper.getCurrentOrNextBusinessDay(localDate);
        Assert.assertEquals(currentOrNextBusinessDay, localDate);
    }

    @Test
    public void getCurrentOrNextBusinessDayReturnsNextBusinessDay() {
        LocalDate localDate = LocalDate.of(2021, 2, 6);
        LocalDate currentOrNextBusinessDay = frenchHelper.getCurrentOrNextBusinessDay(localDate);
        Assert.assertEquals(currentOrNextBusinessDay, LocalDate.of(2021, 2, 8));
    }

    @Test
    public void testGetTransferDateBeforeCuttOffTimeAndDateNotProvided_beforeCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 00:00, way before cutoff
        swedishHelper.setClock(fixedClock("2020-03-19T00:00:00.00Z"));
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(null, 23, 59);
        String parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transferDate);
        Assert.assertNotNull(transferDate);
        assertTrue(swedishHelper.isBusinessDay(transferDate));
        Assert.assertEquals("2020-03-19", parsedDate);
    }

    @Test
    public void testGetTransferDateAfterCuttOffTimeAndDateNotProvided_afterCutOffTime() {
        // 19/march is a Thursday and it's 03:30, after cutoff
        swedishHelper.setClock(fixedClock("2020-03-19T03:30:00.00Z"));
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(null, 00, 00);
        String parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transferDate);
        Assert.assertNotNull(transferDate);
        assertTrue(swedishHelper.isBusinessDay(transferDate));
        Assert.assertEquals("2020-03-20", parsedDate);
    }

    @Test
    public void testGetTransferDateAfterCuttOffTimeAndDateNotProvided_atCutOffTime() {
        // 19/march is a Thursday and it's 23:59, at cutoff
        swedishHelper.setClock(fixedClock("2020-03-19T23:59:59.00Z"));
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(null, 23, 59);
        String parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transferDate);
        Assert.assertNotNull(transferDate);
        assertTrue(swedishHelper.isBusinessDay(transferDate));
        Assert.assertEquals("2020-03-20", parsedDate);
    }

    @Test
    public void testGetTransferDateAfterCuttOffTimeAndDateNotProvided_onFriday_afterCutOffTime() {
        // 20/march is a Friday and it's 03:30, after cutoff
        swedishHelper.setClock(fixedClock("2020-03-20T03:30:00.00Z"));
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(null, 00, 00);
        String parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transferDate);
        Assert.assertNotNull(transferDate);
        assertTrue(swedishHelper.isBusinessDay(transferDate));
        Assert.assertEquals("2020-03-23", parsedDate);
    }

    @Test
    public void testGetTransferDateAfterCuttOffTimeAndDateNotProvided_onNonBusinessDay() {
        // 21/march is a Saturday cutoff time does not matter
        swedishHelper.setClock(fixedClock("2020-03-21T23:59:00.00Z"));
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(null, 00, 00);
        String parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transferDate);
        Assert.assertNotNull(transferDate);
        assertTrue(swedishHelper.isBusinessDay(transferDate));
        Assert.assertEquals("2020-03-23", parsedDate);
    }

    @Test
    public void testGetTransferDateBeforeCuttOffTimeAndDateProvided() {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(ZoneId.of("CET"))
                                .toInstant());
        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(anyDate, 23, 59);
        assertEquals(transferDate, anyDate);
    }

    @Test
    public void testGetTransferDateAfterCuttOffTimeAndDateProvided() {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(ZoneId.of("CET"))
                                .toInstant());

        Date transferDate = swedishHelper.getProvidedDateOrBestPossibleDate(anyDate, 00, 00);
        assertEquals(transferDate, anyDate);
    }

    @Test
    public void calculateIfWithinCutOffTimeReturnsTrue() {
        ZonedDateTime zonedDateTime =
                ZonedDateTime.of(LocalDate.now(), LocalTime.of(17, 20), ZoneId.of("CET"));
        assertTrue(frenchHelper.calculateIfWithinCutOffTime(zonedDateTime, 17, 30, 900));
    }

    @Test
    public void calculateIfWithinCutOffTimeReturnsFalse() {
        ZonedDateTime zonedDateTime =
                ZonedDateTime.of(LocalDate.now(), LocalTime.of(17, 14), ZoneId.of("CET"));
        assertFalse(frenchHelper.calculateIfWithinCutOffTime(zonedDateTime, 17, 30, 900));
    }
}
