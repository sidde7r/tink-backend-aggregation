package se.tink.backend.aggregation.agents.banks.sbab.util;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SBABDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));

    private Clock fixedClock(String moment) {
        Instant instant = Instant.parse(moment);
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForInternalTransfer()
            throws ParseException {
        // 19/march is a Thursday and it's 03:30, way before cutoff
        SBABDateUtil.setClock(fixedClock("2020-03-19T03:30:00.00Z"));
        String transferDate = SBABDateUtil.getTransferDate(null, true);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        assertTrue(dateHelper.isBusinessDay(parsedDate));
        Assert.assertEquals("2020-03-19", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForExternalTransfer_beforeCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 03:30, way before cutoff
        SBABDateUtil.setClock(fixedClock("2020-03-19T03:30:00.00Z"));
        String transferDate = SBABDateUtil.getTransferDate(null, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        Assert.assertEquals("2020-03-19", transferDate);
    }

    @Test
    public void testTransferDateIsABusinessDayForExternalTransfer_pastCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 20:30, past cutoff date
        SBABDateUtil.setClock(fixedClock("2020-03-19T20:30:00.00Z"));
        String transferDate = SBABDateUtil.getTransferDate(null, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        assertTrue(dateHelper.isBusinessDay(parsedDate));
        // The date returned from the DateUtil should be friday 20 because it's the immediately next
        // business day
        Assert.assertEquals("2020-03-20", transferDate);
    }

    @Test
    public void testTransferDateIsABusinessDayForExternalTransfer_weekendCase()
            throws ParseException {
        // 20/march is a Friday and it's 20:30, past cutoff date
        SBABDateUtil.setClock(fixedClock("2020-03-20T20:30:00.00Z"));
        String transferDate = SBABDateUtil.getTransferDate(null, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        assertTrue(dateHelper.isBusinessDay(parsedDate));
        // The date returned from the DateUtil should be monday 23 because it's the immediately next
        // business day (friday 20 is already past cut-off time and 21 and 22 are weekend days)
        Assert.assertEquals("2020-03-23", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsNotChangedWhenExplictlySetForInternalTransfer()
            throws ParseException {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(ZoneId.of("CET"))
                                .toInstant());
        String transferDate = SBABDateUtil.getTransferDate(anyDate, true);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertEquals(anyDate, parsedDate);
    }

    @Test
    public void testTransferExecutionDateIsNotChangedWhenExplictlySetForExternalTransfer()
            throws ParseException {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(ZoneId.of("CET"))
                                .toInstant());
        String transferDate = SBABDateUtil.getTransferDate(anyDate, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertEquals(anyDate, parsedDate);
    }
}
