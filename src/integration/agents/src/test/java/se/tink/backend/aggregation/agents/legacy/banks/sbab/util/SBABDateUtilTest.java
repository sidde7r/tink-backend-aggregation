package se.tink.backend.aggregation.agents.banks.sbab.util;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
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

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSet() throws ParseException {
        String transferDate = SBABDateUtil.getTransferDate(null, true);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForInternalTransfer()
            throws ParseException {
        String transferDate = SBABDateUtil.getTransferDate(null, true);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        assertTrue(dateHelper.isBusinessDay(parsedDate));
    }

    @Test
    public void testTransferExecutionDateIsDefaultedWhenNotSetForExternalTransfer()
            throws ParseException {
        String transferDate = SBABDateUtil.getTransferDate(null, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
    }

    @Test
    public void testTransferDateIsABusinessDayForExternalTransfer() throws ParseException {
        String transferDate = SBABDateUtil.getTransferDate(null, false);
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(transferDate);
        Assert.assertNotNull(parsedDate);
        assertTrue(dateHelper.isBusinessDay(parsedDate));
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
