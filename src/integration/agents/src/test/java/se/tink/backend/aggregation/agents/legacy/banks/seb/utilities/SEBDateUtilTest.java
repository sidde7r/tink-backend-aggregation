package se.tink.backend.aggregation.agents.legacy.banks.seb.utilities;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

public class SEBDateUtilTest {

    // "Till andra svenska banker är pengarna tillgängliga för mottagaren samma dag om du skickar
    // uppdraget senast
    // 13.35." // SEB's Web Internet Bank
    private static final int MIDDAY_HOUR = 13;
    private static final int MIDDAY_MINUTE = 25;
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    private Clock fixedClock(Calendar calendar) {
        final Instant instant = calendar.toInstant();
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testTransfersDatesAreNotMovedForInternalTransfersWhenNotSet() {
        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.APRIL, 3, 0, 0, 0);

        for (int hour : new int[] {10, 15}) {
            for (int dayOfWeek : new int[] {0, 1, 2, 3, 4, 5, 6}) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                Date now = cal.getTime();
                SEBDateUtil.setClock(fixedClock(cal));

                String transferDate = SEBDateUtil.getTransferDate(null, true);

                final DateTimeFormatter pattern = DateTimeFormat.forPattern("YYYY-MM-dd");
                Assert.assertEquals(pattern.print(now.getTime()), transferDate);
            }
        }
    }

    private void assertTimeOfDayIsBeforeMidday(Calendar calendar) {
        Assert.assertTrue(
                calendar.get(Calendar.HOUR_OF_DAY) < MIDDAY_HOUR
                        || (calendar.get(Calendar.HOUR_OF_DAY) == MIDDAY_HOUR
                                && calendar.get(Calendar.MINUTE) < MIDDAY_MINUTE));
    }

    private void assertTimeOfDayIsAfterMidday(Calendar calendar) {
        Assert.assertTrue(
                calendar.get(Calendar.HOUR_OF_DAY) > MIDDAY_HOUR
                        || (calendar.get(Calendar.HOUR_OF_DAY) == MIDDAY_HOUR
                                && calendar.get(Calendar.MINUTE) > MIDDAY_MINUTE));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenSaturdayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 10, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.SATURDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        cal.add(Calendar.DAY_OF_YEAR, 2);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenSaturdayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 15, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.SATURDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        cal.add(Calendar.DAY_OF_YEAR, 2);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenSundayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 14, 10, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        cal.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenSundayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 14, 15, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        cal.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsNotMovedToNextBusinessDayWhenMondayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 11, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Assert.assertEquals("2016-02-15", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenMondayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 15, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Assert.assertEquals("2016-02-16", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsNotMovedToNextBusinessDayWhenFridayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.MARCH, 11, 11, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Assert.assertEquals("2016-03-11", SEBDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testTransferDateIsMovedToNextBusinessDayWhenFridayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.MARCH, 11, 15, 0, 0);

        SEBDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Assert.assertEquals("2016-03-14", SEBDateUtil.getTransferDate(null, false));
    }
}
