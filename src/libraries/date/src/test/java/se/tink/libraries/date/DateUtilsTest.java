package se.tink.libraries.date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pojava.datetime.DateTimeConfig;

@RunWith(JUnitParamsRunner.class)
public class DateUtilsTest {

    private final DateFormat df8 = new SimpleDateFormat("yyyyMMdd");
    private final DateFormat df6 = new SimpleDateFormat("yyMMdd");
    private static final String TZ = "Europe/Stockholm";
    private DateTimeZone jodaDefault = DateTimeZone.getDefault();
    private DateTimeConfig pojaDefault = DateTimeConfig.getGlobalDefault();
    private TimeZone jvmDefault = TimeZone.getDefault();

    @Before
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(TZ));
        DateTimeZone.setDefault(DateTimeZone.forID(TZ));
        DateTimeConfig dateTimeConfig = new DateTimeConfig();
        dateTimeConfig.setInputTimeZone(TimeZone.getTimeZone(TZ));
        DateTimeConfig.setGlobalDefault(dateTimeConfig);
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(jvmDefault);
        DateTimeZone.setDefault(jodaDefault);
        DateTimeConfig.setGlobalDefault(pojaDefault);
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

        String date8 = df8.format(cal.getTime());
        String date6 = df6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("19" + date6, parsedDate8);
        assertEquals("19" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithThreeYearsAgo() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -3);

        String date8 = df8.format(cal.getTime());
        String date6 = df6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
    }

    // TODO: https://tinkab.atlassian.net/browse/CATS-607
    // This test is non-hermetic. It seems that it would always fail at night between 00:00-02:00,
    // summer time. Most likely related to not taking timezones into account.
    // Rewrite this test so that neither the test nor the method under test depends on the
    // current time.
    @Ignore
    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithOneDayAhead() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);

        String date8 = df8.format(cal.getTime());
        String date6 = df6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("19" + date6, parsedDate8);
        assertEquals("19" + date6, parsedDate6);
    }

    @Test
    public void testTurnPastSixDigitsDateIntoEightDigitsWithOneDayAgo() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        String date8 = df8.format(cal.getTime());
        String date6 = df6.format(cal.getTime());
        String parsedDate8 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date8);
        String parsedDate6 = DateUtils.turnPastSixDigitsDateIntoEightDigits(date6);

        assertEquals("20" + date6, parsedDate8);
        assertEquals("20" + date6, parsedDate6);
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
        "2017-08-21, 2017-11-21, 3",
        "2017-08-21, 2017-08-25, 0",
        "2017-12-31, 2018-01-01, 1",
        "2017-12-05, 2018-01-31, 1",
        "2017-12-05, 2018-02-04, 2",
        "2019-01-05, 2016-12-05, -25"
    })
    public void getCalendarMonthsBetween(String date1Raw, String date2Raw, Long expected) {
        Date date1 = DateTime.parse(date1Raw).toDate();
        Date date2 = DateTime.parse(date2Raw).toDate();

        Long actual = DateUtils.getCalendarMonthsBetween(date1, date2);

        assertEquals(expected, actual);
    }
}
