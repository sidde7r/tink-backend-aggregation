package se.tink.libraries.date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pojava.datetime.DateTimeConfig;

@RunWith(JUnitParamsRunner.class)
public class DateUtilsTest {

    static final DateFormat DF8 = new SimpleDateFormat("yyyyMMdd");
    static final DateFormat DF6 = new SimpleDateFormat("yyMMdd");
    private static final String TZ = "Europe/Stockholm";
    private DateTimeZone jodaDefault = DateTimeZone.getDefault();
    private DateTimeConfig pojaDefault = DateTimeConfig.getGlobalDefault();
    private TimeZone jvmDefault = TimeZone.getDefault();

    @Before
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(TZ));
        DateTimeZone.setDefault(DateTimeZone.forID(TZ));
        DateTimeConfig.setGlobalDefault(
                new DateTimeConfig() {
                    {
                        setInputTimeZone(TimeZone.getTimeZone(TZ));
                    }
                });
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(jvmDefault);
        DateTimeZone.setDefault(jodaDefault);
        DateTimeConfig.setGlobalDefault(pojaDefault);
    }

    @Test
    public void testBenchmarkGetMonthPeriod() {
        long startTime = System.currentTimeMillis();
        long maxTestTime = TimeUnit.SECONDS.toMillis(30);

        int VERY_RANDOM_SEED = 42;
        Random random = new Random(VERY_RANDOM_SEED);

        int counter = 0;
        while ((System.currentTimeMillis() - startTime) < maxTestTime) {
            Date date =
                    new Date(
                            2005 - 1900 + random.nextInt(10),
                            random.nextInt(12),
                            1 + random.nextInt(27));
            DateUtils.getMonthPeriod(date, ResolutionTypes.MONTHLY_ADJUSTED, 25);
            counter++;
        }

        System.out.println("Executed " + counter + " iterations.");
    }

    @Test
    public void belgianDateUtilTest() {
        String date = "2018-12-24";
        String kingsDay = "2018-04-02";
        CountryDateUtils be = CountryDateUtils.getBelgianDateUtils();
        CountryDateUtils sv = CountryDateUtils.getSwedishDateUtils();
        Date date1 = new DateTime(DateTime.parse(date)).toDate();
        Date date2 = new DateTime(DateTime.parse(kingsDay)).toDate();
        assertEquals(be.isBusinessDay(date1), true);
        assertFalse(sv.isBusinessDay(date1));
        assertFalse(be.isBusinessDay(date2));
        assertFalse(sv.isBusinessDay(date2));
    }

    @Test
    public void testGetFirstDateFromPeriods() throws ParseException {
        Date isDate =
                DateUtils.getFirstDateFromPeriods(
                        Lists.newArrayList("2012-12", "2013-01", "2012-11"),
                        ResolutionTypes.MONTHLY,
                        25);
        Date shouldDate = DateUtils.parseDate("2012-11-01");

        System.out.println(isDate.toString());
        System.out.println(shouldDate.toString());

        assertTrue(isDate.getTime() == shouldDate.getTime());
    }

    @Test
    public void testGetMonthPeriod() throws ParseException {
        Date date = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2016-05-27");
        assertEquals("2016-05", DateUtils.getMonthPeriod(date));
        assertEquals("2016-05", DateUtils.getMonthPeriod(date, ResolutionTypes.MONTHLY, 25));
        assertEquals(
                "2016-06", DateUtils.getMonthPeriod(date, ResolutionTypes.MONTHLY_ADJUSTED, 25));
    }

    @Test
    public void testGetFirstDateFromPeriodsAdjusted() {
        Date isDate =
                DateUtils.getFirstDateFromPeriods(
                        Lists.newArrayList("2012-12", "2013-01", "2012-11"),
                        ResolutionTypes.MONTHLY_ADJUSTED,
                        25);
        Date shouldDate = DateUtils.parseDate("2012-10-25");

        System.out.println(isDate.toString());
        System.out.println(shouldDate.toString());

        assertTrue(isDate.getTime() == shouldDate.getTime());
    }

    @Test
    public void testGetCurrentMonthPeriodProgress() {
        double progress = DateUtils.getCurrentMonthPeriodProgress(ResolutionTypes.MONTHLY, -1);

        System.out.println("MONTHLY: " + progress);

        assertNotEquals(progress, 0);

        double progressAdjusted =
                DateUtils.getCurrentMonthPeriodProgress(ResolutionTypes.MONTHLY_ADJUSTED, 25);

        System.out.println("MONTHLY_ADJUSTED: " + progressAdjusted);

        assertNotEquals(progressAdjusted, 0);
    }

    @Test
    public void testGetLastDateFromPeriods() {
        Date isDate =
                DateUtils.getLastDateFromPeriods(
                        Lists.newArrayList("2012-12", "2013-01", "2012-11"),
                        ResolutionTypes.MONTHLY,
                        25);

        Calendar shouldCalendar = DateUtils.getCalendar();
        shouldCalendar.setTime(DateUtils.parseDate("2013-01-31"));
        DateUtils.setInclusiveEndTime(shouldCalendar);

        Date shouldDate = shouldCalendar.getTime();

        System.out.println(isDate.toString());
        System.out.println(shouldDate.toString());

        assertTrue(isDate.getTime() == shouldDate.getTime());
    }

    @Test
    public void testHolidays() {
        // Nyårsdagen 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-01-01")));
        // Trettondagen 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-01-06")));
        // Nationaldagen 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-06-06")));
        // Midsommardagen 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-06-22")));
        // Midsommarafton 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-06-21")));
        // Julafton 2013
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2013-12-24")));
        // Day before Julafton 2013
        assertTrue(DateUtils.isBusinessDay(DateUtils.parseDate("2013-12-23")));

        // Nyårsdagen 2014
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2014-01-01")));
        // Trettondagen 2014
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2014-01-06")));
        // Nationaldagen 2014
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2014-06-06")));
        // Midsommardagen 2014
        assertFalse(DateUtils.isBusinessDay(DateUtils.parseDate("2014-06-21")));
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithFirstOf2014() throws Exception {

        String date8 = "20140101";
        String date6 = "140101";
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithDaysAroundMilleniumShift()
            throws Exception {
        String date8 = "19991231";
        String date6 = "991231";
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("19" + date6, parsedDate8);
        assertEquals("19" + date6, parsedDate6);

        date8 = "20000101";
        date6 = "000101";
        parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithThreeYearsAhead() throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 3);

        String date8 = DF8.format(cal.getTime());
        String date6 = DF6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("19" + date6, parsedDate8);
        assertEquals("19" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithThreeYearsAgo() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -3);

        String date8 = DF8.format(cal.getTime());
        String date6 = DF6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithOneDayAhead() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);

        String date8 = DF8.format(cal.getTime());
        String date6 = DF6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("19" + date6, parsedDate8);
        assertEquals("19" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithOneDayAgo() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        String date8 = DF8.format(cal.getTime());
        String date6 = DF6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
    }

    @Test
    public void testToISO8601Format() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 18);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 58);
        cal.set(Calendar.SECOND, 38);

        assertEquals("2014-02-18T23:58:38Z", DateUtils.toISO8601Format(cal.getTime()));
    }

    @Test
    public void testDaysBetween() {

        assertEquals(0, DateUtils.daysBetween(new Date(), new Date()));
        assertEquals(
                0,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2011-11-11 11:11:11"),
                        DateUtils.parseDate("2011-11-11 17:17:17")));

        assertEquals(
                1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2011-11-11"), DateUtils.parseDate("2011-11-12")));
        assertEquals(
                1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2011-11-11 23:59:59"),
                        DateUtils.parseDate("2011-11-12 00:00:00")));
        assertEquals(
                1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2014-12-31"), DateUtils.parseDate("2015-01-01")));

        assertEquals(
                -1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2011-11-12"), DateUtils.parseDate("2011-11-11")));
        assertEquals(
                -1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2011-11-12 00:00:00"),
                        DateUtils.parseDate("2011-11-11 23:59:59")));
        assertEquals(
                -1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2015-01-01"), DateUtils.parseDate("2014-12-31")));
    }

    @Test
    public void testDaysBetweenForDaylightSavings() {
        // Morning 2015-03-29 Sweden went from winter time to summer time.
        assertEquals(
                1,
                DateUtils.daysBetween(
                        DateUtils.parseDate("2015-03-29"), DateUtils.parseDate("2015-03-30")));
    }

    @Test
    @Parameters({
        "2011-11-11 00:00:00, 2011-11-11 23:59:59, true",
        "2011-11-10 23:59:59, 2011-11-11 00:00:00, false"
    })
    public void isSameDay(String date1, String date2, boolean isSameDay) {
        assertEquals(
                isSameDay,
                DateUtils.isSameDay(DateUtils.parseDate(date1), DateUtils.parseDate(date2)));
    }

    @Test
    public void testMinMax() {
        assertEquals(
                DateUtils.parseDate("2011-11-12"),
                DateUtils.min(
                        DateUtils.parseDate("2011-11-12"), DateUtils.parseDate("2011-11-12")));

        assertEquals(
                DateUtils.parseDate("2013-11-12"),
                DateUtils.min(
                        DateUtils.parseDate("2013-11-12"), DateUtils.parseDate("2014-11-12")));

        assertEquals(
                DateUtils.parseDate("2013-11-12"),
                DateUtils.min(
                        DateUtils.parseDate("2014-11-12"), DateUtils.parseDate("2013-11-12")));

        assertNull(DateUtils.min(null, DateUtils.parseDate("2013-11-12")));

        assertNull(DateUtils.min(null, null));

        assertEquals(
                DateUtils.parseDate("2011-11-12"),
                DateUtils.max(
                        DateUtils.parseDate("2011-11-12"), DateUtils.parseDate("2011-11-12")));

        assertEquals(
                DateUtils.parseDate("2014-11-12"),
                DateUtils.max(
                        DateUtils.parseDate("2013-11-12"), DateUtils.parseDate("2014-11-12")));

        assertEquals(
                DateUtils.parseDate("2014-11-12"),
                DateUtils.max(
                        DateUtils.parseDate("2014-11-12"), DateUtils.parseDate("2013-11-12")));

        assertEquals(
                DateUtils.parseDate("2013-11-12"),
                DateUtils.max(null, DateUtils.parseDate("2013-11-12")));

        assertNull(DateUtils.max(null, null));
    }

    /** Verifies that we can calculate the month period for all dates */
    @Test
    public void monthPeriodsShouldBePossibleToCalculate() {
        DateTime startDate = new DateTime(2000, 1, 1, 0, 0); // Jan 1 2000
        DateTime endDate = new DateTime().plusYears(5); // Leap year guaranteed to be included

        DateTime dt = startDate;

        for (Integer breakDate : getPeriodBreakDateList()) {

            while (dt.isBefore(endDate)) {

                String period =
                        DateUtils.getMonthPeriod(
                                dt.toDate(), ResolutionTypes.MONTHLY_ADJUSTED, breakDate);

                assertNotNull("Could not calculate month period", period);

                dt = dt.plusDays(1);
            }
            dt = startDate;
        }
    }

    /**
     * Verifies that month period calculations are correct by, 1) Extracting the first and last date
     * of a period 2) Verifies that each date that is between the first and last date gets the same
     * period
     */
    @Test
    public void verifyAllMonthlyBreakDates() {

        ResolutionTypes resolution = ResolutionTypes.MONTHLY_ADJUSTED;
        int fiveYearsAhead =
                new DateTime()
                        .plusYears(5)
                        .get(DateTimeFieldType.year()); // Leap year guaranteed to be included

        for (int year = 2000; year <= fiveYearsAhead; year++) {
            for (int month = 1; month <= 12; month++) {
                String period = String.format("%d-%02d", year, month);

                for (Integer breakDate : getPeriodBreakDateList()) {
                    DateTime startDate =
                            new DateTime(
                                    DateUtils.getFirstDateFromPeriod(
                                            period, resolution, breakDate));
                    DateTime endDate =
                            new DateTime(
                                    DateUtils.getLastDateFromPeriod(period, resolution, breakDate));

                    DateTime dt = startDate;

                    while (dt.isBefore(endDate)) {

                        // Verify that a date that is in between the start and end date of a period
                        // has the same period
                        assertEquals(
                                period,
                                DateUtils.getMonthPeriod(dt.toDate(), resolution, breakDate));

                        dt = dt.plusDays(1);
                    }
                }
            }
        }
    }

    /** Get a list of the valid monthly break dates in Tink */
    private List<Integer> getPeriodBreakDateList() {
        List<Integer> periodBreakDateList = Lists.newArrayList();

        for (int i = 1; i <= 31; i++) {
            periodBreakDateList.add(i);
        }

        return periodBreakDateList;
    }

    @Test
    public void verifyNextMonthPeriod() {
        assertEquals("2015-12", DateUtils.getNextMonthPeriod("2015-11"));
        assertEquals("2016-01", DateUtils.getNextMonthPeriod("2015-12"));
    }

    @Test
    public void januaryYearZero_HasNextFebruaryYearZero() {
        assertThat(DateUtils.getNextMonthPeriod("0000-01")).isEqualTo("0000-02");
    }

    /**
     * Months after 1899-11 was returned correctly before, so make sure we cover below that since
     * 1899-11 was broken
     */
    @Test
    public void edgeCaseNovemberYear1899_HasNextDecember1899() {
        assertThat(DateUtils.getNextMonthPeriod("1899-11")).isEqualTo("1899-12");
    }

    /** Test that ensures our month periods are correctly calculated for year 0 -> 3000 */
    @Test
    public void ensureNextMonthPeriodForAllPeriods() {
        // Loop from year 0 up to year 3000 (12 months * 3000 years = 36000 iteratinos)
        for (int i = 0; i < 36000; i++) {
            String yearMonth = getMonthStringSinceYearZero(i);
            String nextMonthPeriod = DateUtils.getNextMonthPeriod(yearMonth);

            String expectedMonthString = getMonthStringSinceYearZero(i + 1);
            assertThat(nextMonthPeriod).isEqualTo(expectedMonthString);
        }
    }

    /**
     * E.g. nthMonth = 10 -> the 10th month in year 0 --> November year 0 nthMonth = 20 -> the 20th
     * month since year 0 --> 8th month in year 1 --> September year 1
     */
    private String getMonthStringSinceYearZero(int nthMonth) {
        int year = nthMonth / 12;
        int month = (nthMonth % 12) + 1;
        return String.format("%04d-%02d", year, month);
    }

    @Test(expected = RuntimeException.class)
    public void verifyNextMonthPeriodInvalidFormat() {
        DateUtils.getNextMonthPeriod("2015-11-01");
    }

    @Test
    public void verifyPreviousMonthPeriod() {
        assertEquals("2015-11", DateUtils.getPreviousMonthPeriod("2015-12"));
        assertEquals("2015-12", DateUtils.getPreviousMonthPeriod("2016-01"));
    }

    /** Not useful perhaps, but should still work if we'd ever do it */
    @Test
    public void januaryYearZero_HasPreviousDecemberYearMinusOne() {
        assertThat(DateUtils.getPreviousMonthPeriod("0000-01")).isEqualTo("-0001-12");
    }

    /** Test that ensures our month periods are correctly calculated for year 0 -> 3000 */
    @Test
    public void ensurePreviousMonthPeriodForAllPeriods() {
        // Loop from year 0 up to year 3000 (12 months * 3000 years = 36000 iteratinos)
        for (int i = 1; i < 36000; i++) {
            String yearMonth = getMonthStringSinceYearZero(i);
            String previousMonthPeriod = DateUtils.getPreviousMonthPeriod(yearMonth);

            String expectedMonthString = getMonthStringSinceYearZero(i - 1);
            assertThat(previousMonthPeriod).isEqualTo(expectedMonthString);
        }
    }

    @Test(expected = RuntimeException.class)
    public void verifyPreviousMonthPeriodInvalidFormat() {
        DateUtils.getPreviousMonthPeriod("2015-11-01");
    }

    @Test
    public void testDateList() {
        Date last = new Date();
        Date first = org.apache.commons.lang3.time.DateUtils.addDays(last, -5);

        List<Date> dates = DateUtils.createDailyDateList(first, last);

        assertEquals(6, dates.size());
        assertEquals(
                ThreadSafeDateFormat.FORMATTER_DAILY.format(last),
                ThreadSafeDateFormat.FORMATTER_DAILY.format(dates.get(5)));
    }

    @Test
    public void testDateListReversed() {
        Date last = new Date();
        Date first = org.apache.commons.lang3.time.DateUtils.addDays(last, -5);

        List<Date> dates = DateUtils.createDailyDateList(first, last, true);

        assertEquals(6, dates.size());
        assertEquals(
                ThreadSafeDateFormat.FORMATTER_DAILY.format(last),
                ThreadSafeDateFormat.FORMATTER_DAILY.format(dates.get(0)));
    }

    @Test
    public void testModifiedBuilder() {
        Date last = new Date();
        Date first = org.apache.commons.lang3.time.DateUtils.addDays(last, -5);

        List<Date> dates = DateUtils.createDailyDateList(first, last, true);

        assertEquals(6, dates.size());
        assertEquals(
                ThreadSafeDateFormat.FORMATTER_DAILY
                        .toBuilder()
                        .setLocale(new Locale("fr_FR"))
                        .build()
                        .format(last),
                ThreadSafeDateFormat.FORMATTER_DAILY.format(dates.get(0)));
    }

    @Test
    public void testNonBankingDays() throws ParseException {
        List<String> nonBankDays =
                Lists.newArrayList(
                        "2016-03-25",
                        "2016-03-28",
                        "2016-05-01",
                        "2016-05-05",
                        "2016-06-06",
                        "2016-06-24",
                        "2016-12-24",
                        "2016-12-25",
                        "2016-12-26",
                        "2016-12-31");

        for (String nonBankDay : nonBankDays) {
            assertFalse(
                    nonBankDay + " should be a non business day, but it isn'.",
                    DateUtils.isBusinessDay(
                            ThreadSafeDateFormat.FORMATTER_DAILY.parse(nonBankDay)));
        }
    }

    @Test
    public void testCurrentOrNextBusinessDayToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new DateTime("2016-03-18").toDate());

        Calendar referenceCalendar = (Calendar) calendar.clone();

        Date nextBusinessDate = DateUtils.getCurrentOrNextBusinessDay(calendar.getTime());
        assertTrue(
                org.apache.commons.lang3.time.DateUtils.isSameDay(
                        nextBusinessDate, referenceCalendar.getTime()));
    }

    @Test
    public void testCurrentOrNextBusinessDayOnMonday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new DateTime("2016-03-19").toDate());

        Date nextBusinessDate = DateUtils.getCurrentOrNextBusinessDay(calendar.getTime());
        assertTrue(
                org.apache.commons.lang3.time.DateUtils.isSameDay(
                        nextBusinessDate, DateUtils.addDays(calendar.getTime(), 2)));
    }

    @Test
    public void testGetPreviousPeriod() {

        Period first = new Period();
        first.setName("first");
        first.setResolution(ResolutionTypes.MONTHLY);
        first.setStartDate(new DateTime(2016, 1, 1, 0, 0, 0).toDate());
        first.setEndDate(new DateTime(2016, 1, 31, 23, 59, 59).toDate());

        Period second = new Period();
        second.setName("second");
        second.setResolution(ResolutionTypes.MONTHLY);
        second.setStartDate(new DateTime(2016, 2, 1, 0, 0, 0).toDate());
        second.setEndDate(new DateTime(2016, 2, 29, 23, 59, 59).toDate());

        Period third = new Period();
        third.setName("third");
        third.setResolution(ResolutionTypes.MONTHLY);
        third.setStartDate(new DateTime(2016, 3, 1, 0, 0, 0).toDate());
        third.setEndDate(new DateTime(2016, 3, 31, 23, 59, 59).toDate());

        ImmutableList<Period> periods = ImmutableList.of(first, second, third);

        // Null if no input
        assertNull(DateUtils.getPreviousPeriod(periods, null));

        // No period before the first period
        assertNull(DateUtils.getPreviousPeriod(periods, first));

        // First period before the second
        assertEquals(first, DateUtils.getPreviousPeriod(periods, second));

        // Second period before the third
        assertEquals(second, DateUtils.getPreviousPeriod(periods, third));
    }

    @Test
    public void testDaysFromNow() {
        Date tomorrow1 = DateUtils.daysFromNow(1);
        Date tomorrow2 = DateUtils.inclusiveEndTime(DateUtils.addDays(new Date(), 1));

        assertEquals(tomorrow1, tomorrow2);
    }

    @Test
    public void testIsBusinessDayMoreThanDaysFromNow_dateOutOfRange() {
        Date date = DateUtils.daysFromNow(366);

        assertFalse(DateUtils.isBusinessDayWithinDaysFromNow(date, 365));
    }

    @Test
    public void testIsBusinessDayMoreThanDaysFromNow_dateWithinRange() {
        // Subtract 10 days from a year to ensure holidays can't fail the test
        Date date = DateUtils.daysFromNow(355);

        assertTrue(DateUtils.isBusinessDayWithinDaysFromNow(date, 365));
    }

    @Test
    public void testIsBusinessDayMoreThanDaysFromNow_dateOutOfRangeForBusinessDay() {
        // Add 10 days to ensure holidays can't fail the test
        Date oneYearFromNow = DateUtils.daysFromNow(365 + 10);
        Date businessDay = DateUtils.getCurrentOrPreviousBusinessDay(oneYearFromNow);

        assertFalse(DateUtils.isBusinessDayWithinDaysFromNow(businessDay, 365));
    }

    @Test
    public void testIsBusinessDayMoreThanDaysFromNow_dateWithinRangeForBusinessDay() {
        Date oneYearFromNow = DateUtils.daysFromNow(365);
        Date businessDay =
                DateUtils.addDays(DateUtils.getCurrentOrPreviousBusinessDay(oneYearFromNow), -1);

        assertTrue(DateUtils.isBusinessDayWithinDaysFromNow(businessDay, 365));
    }

    @Test
    public void testIsBusinessDayMoreThanDaysFromNow_dateAtLimit() {
        Date oneYearFromNow = DateUtils.daysFromNow(365);
        Date date = DateUtils.getCurrentOrPreviousBusinessDay(oneYearFromNow);

        assertFalse(DateUtils.isBusinessDayWithinDaysFromNow(date, 365));
    }

    @Test
    public void testBuildSinglePeriodMonthlyAdjusted_periodEqualsPeriodsName() {
        String period = "2016-03";
        Period generatedPeriod =
                DateUtils.buildMonthlyPeriod(period, ResolutionTypes.MONTHLY_ADJUSTED, 25);
        assertEquals(period, generatedPeriod.getName());
    }

    @Test
    public void testBuildSinglePeriodMonthly_periodEqualsPeriodsName() {
        String period = "2016-03";
        Period generatedPeriod = DateUtils.buildMonthlyPeriod(period, ResolutionTypes.MONTHLY, 25);
        assertEquals(period, generatedPeriod.getName());
    }

    @Test
    public void testBuildSinglePeriodMonthlyAdjusted_startDate() {
        String period = "2016-08";
        Period generatedPeriod =
                DateUtils.buildMonthlyPeriod(period, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        String expectedStartDate = "2016-07-25";
        String startDate =
                ThreadSafeDateFormat.FORMATTER_DAILY.format(generatedPeriod.getStartDate());
        assertEquals(expectedStartDate, startDate);
    }

    @Test
    public void testBuildSinglePeriodMonthlyAdjusted_endDate() {
        String period = "2016-08";
        Period generatedPeriod =
                DateUtils.buildMonthlyPeriod(period, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        String expectedEndDate = "2016-08-24";
        String endDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(generatedPeriod.getEndDate());
        assertEquals(expectedEndDate, endDate);
    }

    @Test
    @Parameters({
        "2017-01-01",
        "2017-01-08",
    })
    public void buildDailyPeriod(String strPeriod) {
        Period period = DateUtils.buildDailyPeriod(strPeriod);

        Date date = DateUtils.parseDate(strPeriod);

        assertEquals(strPeriod, period.getName());
        assertEquals(ResolutionTypes.DAILY, period.getResolution());
        assertEquals(DateUtils.setInclusiveStartTime(date), period.getStartDate());
        assertEquals(DateUtils.setInclusiveEndTime(date), period.getEndDate());
    }

    @Test
    @Parameters({
        "2017:1, en, 2017-01-01, 2017-01-07",
        "2017:1, sv, 2017-01-02, 2017-01-08",
    })
    public void buildWeeklyPeriod(
            String strPeriod, String lang, String expStartDate, String expEndDate) {
        Period period = DateUtils.buildWeeklyPeriod(strPeriod, Locale.forLanguageTag(lang));
        assertEquals(strPeriod, period.getName());
        assertEquals(ResolutionTypes.WEEKLY, period.getResolution());
        assertEquals(
                DateUtils.setInclusiveStartTime(DateUtils.parseDate(expStartDate)),
                period.getStartDate());
        assertEquals(
                DateUtils.setInclusiveEndTime(DateUtils.parseDate(expEndDate)),
                period.getEndDate());
    }

    @Test
    @Parameters({
        "2017, MONTHLY, 25, 2017-01-01, 2017-12-31",
        "2017, MONTHLY_ADJUSTED, 25, 2016-12-23, 2017-12-21",
        "2018, MONTHLY_ADJUSTED, 25, 2017-12-22, 2018-12-20",
    })
    public void buildYearlyPeriod(
            int year,
            ResolutionTypes periodMode,
            int periodBreakDay,
            String expStartDate,
            String expEndDate) {
        Period period = DateUtils.buildYearlyPeriod(year, periodMode, periodBreakDay);
        assertEquals(Integer.toString(year), period.getName());
        assertEquals(ResolutionTypes.YEARLY, period.getResolution());
        assertEquals(
                DateUtils.setInclusiveStartTime(DateUtils.parseDate(expStartDate)),
                period.getStartDate());
        assertEquals(
                DateUtils.setInclusiveEndTime(DateUtils.parseDate(expEndDate)),
                period.getEndDate());
    }

    @Test
    @Parameters({
        "2016, MONTHLY_ADJUSTED, 25, 2016",
        "2016-09, MONTHLY_ADJUSTED, 25, 2016",
        "2016:09, MONTHLY_ADJUSTED, 25, 2016",
        "2016-09-09, MONTHLY_ADJUSTED, 25, 2016",
        "2016-12-30, MONTHLY_ADJUSTED, 25, 2017",
        "2016-12-30, MONTHLY, 25, 2016",
    })
    public void getYearlyPeriod(
            String period, ResolutionTypes resolution, int periodBreakDay, String expPeriod) {
        assertEquals(expPeriod, DateUtils.getYearlyPeriod(period, resolution, periodBreakDay));
    }

    @Test
    public void testCreatePeriodListForYear_monthlyAdjusted() {
        List<String> expectedPeriods = Lists.newArrayList();
        expectedPeriods.add("2016-01");
        expectedPeriods.add("2016-02");
        expectedPeriods.add("2016-03");
        expectedPeriods.add("2016-04");
        expectedPeriods.add("2016-05");
        expectedPeriods.add("2016-06");
        expectedPeriods.add("2016-07");
        expectedPeriods.add("2016-08");
        expectedPeriods.add("2016-09");
        expectedPeriods.add("2016-10");
        expectedPeriods.add("2016-11");
        expectedPeriods.add("2016-12");

        List<String> periods =
                DateUtils.createPeriodListForYear(2016, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        assertEquals(expectedPeriods, periods);
    }

    @Test
    public void testCreatePeriodListForYear_monthly() {
        List<String> expectedPeriods = Lists.newArrayList();
        expectedPeriods.add("2016-01");
        expectedPeriods.add("2016-02");
        expectedPeriods.add("2016-03");
        expectedPeriods.add("2016-04");
        expectedPeriods.add("2016-05");
        expectedPeriods.add("2016-06");
        expectedPeriods.add("2016-07");
        expectedPeriods.add("2016-08");
        expectedPeriods.add("2016-09");
        expectedPeriods.add("2016-10");
        expectedPeriods.add("2016-11");
        expectedPeriods.add("2016-12");

        List<String> periods = DateUtils.createPeriodListForYear(2016, ResolutionTypes.MONTHLY, 25);

        assertEquals(expectedPeriods, periods);
    }

    @Test
    public void testRecreatingInfiniteLoopBug() {
        Date date = new Date(-839602800000L);

        String period = ThreadSafeDateFormat.FORMATTER_MONTHLY.format(date);
        assertEquals("1943-05", period);

        DateUtils.getMonthPeriod(date, ResolutionTypes.MONTHLY_ADJUSTED, 25);
    }

    @Test
    public void prettyFormatTests() {
        assertEquals("10 milliseconds", DateUtils.prettyFormatMillis(10));
        assertEquals("-10 milliseconds", DateUtils.prettyFormatMillis(-10));
    }

    @Test
    public void isWithinClosedIntervalWithMidnightOverlapTest() {

        LocalTime from = LocalTime.parse("23:00");
        LocalTime to = LocalTime.parse("01:00");

        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("22:59:59")))
                .isFalse();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("23:00:00")))
                .isTrue();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("23:15:00")))
                .isTrue();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("01:00:00")))
                .isFalse();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("01:01:00")))
                .isFalse();
    }

    @Test
    public void isWithinClosedIntervalWithoutMidnightOverlapTest() {

        LocalTime from = LocalTime.parse("04:00");
        LocalTime to = LocalTime.parse("05:00");

        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("03:59:59")))
                .isFalse();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("04:00:00")))
                .isTrue();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("04:15:00")))
                .isTrue();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("05:00:00")))
                .isFalse();
        assertThat(DateUtils.isWithinClosedInterval(from, to, LocalTime.parse("05:01:00")))
                .isFalse();
    }

    @Test
    @Parameters({
        "2017-03-26 10:10:10, 2017-03-20 00:00:00", // Sunday -> Monday
        "2017-03-27 10:10:10, 2017-03-27 00:00:00", // Monday -> same day
        "2017-03-28 10:10:10, 2017-03-27 00:00:00", // Tuesday -> Monday
    })
    public void getFirstDateOfWeekDefaultLocale(String date, String expectedDate) {
        Calendar calendar = DateUtils.getCalendar(DateUtils.parseDate(date));

        assertEquals(
                DateUtils.parseDate(expectedDate),
                DateUtils.getFirstDateOfWeek(calendar).getTime());
    }

    @Test
    @Parameters({
        "2017-03-27 10:10:10, 2017-03-26 00:00:00", // Monday -> Sunday
        "2017-03-26 10:10:10, 2017-03-26 00:00:00", // Sunday -> same day
        "2017-03-25 10:10:10, 2017-03-19 00:00:00", // Saturday -> Sunday
    })
    public void getFirstDateOfWeekEnLocale(String date, String expectedDate) {
        Calendar calendar = DateUtils.getCalendar(Locale.ENGLISH);
        calendar.setTime(DateUtils.parseDate(date));

        assertEquals(
                DateUtils.parseDate(expectedDate),
                DateUtils.getFirstDateOfWeek(calendar).getTime());
    }

    private Object[] parametersForCreateMonthlyPeriodList() {
        return new Object[] {
            new Object[] {"2017-06", 0, Collections.emptyList()},
            new Object[] {"2017-02", 1, Lists.newArrayList("2017-02")},
            new Object[] {
                "2017-01",
                5,
                Lists.newArrayList("2016-09", "2016-10", "2016-11", "2016-12", "2017-01")
            },
        };
    }

    @Test
    @Parameters(method = "parametersForCreateMonthlyPeriodList")
    public void createMonthlyPeriodList(String lastPeriod, int size, List<String> result) {
        assertThat(DateUtils.createMonthlyPeriodList(lastPeriod, size))
                .containsOnlyElementsOf(result);
    }

    @Test
    @Parameters({
        "2017-08-21, 2017-11-21, 3",
        "2017-08-21, 2017-08-25, 0",
        "2017-12-31, 2018-01-01, 1",
        "2017-12-05, 2018-01-31, 1",
        "2017-12-05, 2018-02-04, 2",
        "2019-01-05, 2016-12-05, -25"
    })
    public void getCalendarMonthsBetween(String date1Raw, String date2Raw, Long expected) {
        Date date1 = new DateTime(DateTime.parse(date1Raw)).toDate();
        Date date2 = new DateTime(DateTime.parse(date2Raw)).toDate();

        Long actual = DateUtils.getCalendarMonthsBetween(date1, date2);

        assertEquals(expected, actual);
    }
}
