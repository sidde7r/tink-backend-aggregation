package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

public class SebDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");

    private Clock fixedClock(Calendar calendar) {
        final Instant instant = calendar.toInstant();
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testExecutionDatesAreNotMovedWhenNotSetForInternalTransfers() {
        Calendar cal = Calendar.getInstance();
        for (int hour : new int[] {9, 16}) {
            for (int dayOfWeek : new int[] {0, 1, 2, 3, 4, 5, 6}) {
                cal.set(2014, Calendar.APRIL, dayOfWeek, hour, 0, 0);
                SebDateUtil.setClock(fixedClock(cal));

                Date now = cal.getTime();
                String transferDate = SebDateUtil.getTransferDate(null, true);
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd");
                Assert.assertEquals(dateTimeFormatter.print(now.getTime()), transferDate);
            }
        }
    }

    @Test
    public void
            testExecutionDateIsMovedToNextBusinessDayWhenSetOnANonBusinessDayForExternalTransfers() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 10, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDayBeforeCutoffForExternalTransfers() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 10, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDate(null, false));
    }

    @Test
    public void
            testExecutionDateIsMovedToNextBusinessDayWhenSetOnABusinessDayAfterCutoffForExternalTransfers() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 15, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-16", SebDateUtil.getTransferDate(null, false));
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnANonBusinessDayForBgPg() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 16, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDateForBgPg(null));
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDayBeforeCutoffForBgPg() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 8, 30, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDateForBgPg(null));
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnABusinessDayAfterCutoffForBgPg() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 15, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-16", SebDateUtil.getTransferDateForBgPg(null));
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnANonBusinessDayForSepa() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 13, 16, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDateForSepa(null));
    }

    @Test
    public void testExecutionDateIsNotMovedWhenSetOnABusinessDayBeforeCutoffForSepa() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 10, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-15", SebDateUtil.getTransferDateForSepa(null));
    }

    @Test
    public void testExecutionDateIsMovedToNextBusinessDayWhenSetOnABusinessDayAfterCutoffForSepa() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 15, 15, 0, 0);
        SebDateUtil.setClock(fixedClock(cal));

        Assert.assertEquals("2016-02-16", SebDateUtil.getTransferDateForSepa(null));
    }
}
