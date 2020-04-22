package se.tink.backend.aggregation.agents.banks.danskebank.v2.util;

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

public class DanskeBankDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));
    private final DanskeBankDateUtil danskeBankDateUtil =
            new DanskeBankDateUtil(DEFAULT_ZONE_ID, DEFAULT_LOCALE);
    private static final ThreadSafeDateFormat THREAD_SAFE_DATE_FORMAT =
            new ThreadSafeDateFormat("yyyyMMdd");

    private Clock fixedClock(String moment) {
        Instant instant = Instant.parse(moment);
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForInternalTransfer()
            throws ParseException {
        // internal transfers have no cut-off time
        String transferDate =
                danskeBankDateUtil.getTransferDateForInternalTransfer(
                        null, fixedClock("2020-03-19T03:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        Assert.assertEquals("Now", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForExternalTransfer_beforeCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 03:30, way before cutoff
        String transferDate =
                danskeBankDateUtil.getTransferDateForExternalTransfer(
                        null, fixedClock("2020-03-19T03:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        Assert.assertEquals("Now", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForBgPg_beforeCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 03:30, way before cutoff
        String transferDate =
                danskeBankDateUtil.getTransferDateForBgPg(
                        null, fixedClock("2020-03-19T03:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        Assert.assertEquals("Now", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForExternalTransfer_afterCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 20:30, past cutoff date
        String transferDate =
                danskeBankDateUtil.getTransferDateForExternalTransfer(
                        null, fixedClock("2020-03-19T20:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        Assert.assertEquals("20200320", transferDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForBgPg_afterCutOffTime()
            throws ParseException {
        // 19/march is a Thursday and it's 20:30, past cutoff date
        String transferDate =
                danskeBankDateUtil.getTransferDateForBgPg(
                        null, fixedClock("2020-03-19T20:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        Assert.assertEquals("20200320", transferDate);
    }

    @Test
    public void testTransferDateIsABusinessDayForExternalTransfer_weekendCase()
            throws ParseException {
        // 20/march is a Friday and it's 20:30, past cutoff date
        String transferDate =
                danskeBankDateUtil.getTransferDateForExternalTransfer(
                        null, fixedClock("2020-03-20T20:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        // The date returned from the DateUtil should be monday 23 because it's the immediately next
        // business day (friday 20 is already past cut-off time and 21 and 22 are weekend days)
        Assert.assertEquals("20200323", transferDate);
    }

    @Test
    public void testTransferDateIsABusinessDayForBgPg_weekendCase() throws ParseException {
        // 20/march is a Friday and it's 20:30, past cutoff date
        String transferDate =
                danskeBankDateUtil.getTransferDateForBgPg(
                        null, fixedClock("2020-03-20T20:30:00.00Z"));
        Assert.assertNotNull(transferDate);
        // The date returned from the DateUtil should be monday 23 because it's the immediately next
        // business day (friday 20 is already past cut-off time and 21 and 22 are weekend days)
        Assert.assertEquals("20200323", (transferDate));
    }

    @Test
    public void testTransferExecutionDateIsNotChangedWhenExplictlySetForInternalTransfer()
            throws ParseException {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(DEFAULT_ZONE_ID)
                                .toInstant());
        String transferDate = danskeBankDateUtil.getTransferDateForInternalTransfer(anyDate);
        Date parsedDate = THREAD_SAFE_DATE_FORMAT.parse(transferDate);
        Assert.assertEquals(anyDate, parsedDate);
    }

    @Test
    public void testTransferExecutionDateIsNotChangedWhenExplictlySetForExternalTransfer()
            throws ParseException {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(DEFAULT_ZONE_ID)
                                .toInstant());
        String transferDate = danskeBankDateUtil.getTransferDateForExternalTransfer(anyDate);
        Date parsedDate = THREAD_SAFE_DATE_FORMAT.parse(transferDate);
        Assert.assertEquals(anyDate, parsedDate);
    }

    @Test
    public void testTransferExecutionDateIsNotChangedWhenExplictlySetForBgPg()
            throws ParseException {
        Date anyDate =
                Date.from(
                        LocalDate.of(2020, 06, 11)
                                .atStartOfDay()
                                .atZone(DEFAULT_ZONE_ID)
                                .toInstant());
        String transferDate = danskeBankDateUtil.getTransferDateForBgPg(anyDate);
        Date parsedDate = THREAD_SAFE_DATE_FORMAT.parse(transferDate);
        Assert.assertEquals(anyDate, parsedDate);
    }
}
