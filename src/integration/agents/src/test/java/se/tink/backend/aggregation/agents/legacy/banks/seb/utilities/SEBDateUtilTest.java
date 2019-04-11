package se.tink.backend.aggregation.agents.banks.seb.utilities;

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

    @Test
    public void testSEBTransfersDatesAreAlwaysUnaltered() {
        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.APRIL, 3, 0, 0, 0);

        for (int hour : new int[] {10, 15}) {
            for (int dayOfWeek : new int[] {0, 1, 2, 3, 4, 5, 6}) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                Date now = cal.getTime();

                String transferDate = SEBDateUtil.nextPossibleTransferDate(now, true);

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
    public void testTransferDateForExternalOnSaturdayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 10, 0, 0);

        Assert.assertEquals(Calendar.SATURDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Date now = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 2);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnSaturdayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 15, 0, 0);

        Assert.assertEquals(Calendar.SATURDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Date now = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 2);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnSundayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 14, 10, 0, 0);

        Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Date now = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnSundayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 14, 15, 0, 0);

        Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Date now = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));

        Assert.assertEquals("2016-02-15", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnMondayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 11, 0, 0);

        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Date now = cal.getTime();

        Assert.assertEquals("2016-02-15", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnMondayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 15, 0, 0);

        Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Date now = cal.getTime();

        Assert.assertEquals("2016-02-16", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnFridayBeforeMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.MARCH, 11, 11, 0, 0);

        Assert.assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsBeforeMidday(cal);

        Date now = cal.getTime();

        Assert.assertEquals("2016-03-11", SEBDateUtil.nextPossibleTransferDate(now, false));
    }

    @Test
    public void testTransferDateForExternalOnFridayAfterMidday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.MARCH, 11, 15, 0, 0);

        Assert.assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertTimeOfDayIsAfterMidday(cal);

        Date now = cal.getTime();

        Assert.assertEquals("2016-03-14", SEBDateUtil.nextPossibleTransferDate(now, false));
    }
}
