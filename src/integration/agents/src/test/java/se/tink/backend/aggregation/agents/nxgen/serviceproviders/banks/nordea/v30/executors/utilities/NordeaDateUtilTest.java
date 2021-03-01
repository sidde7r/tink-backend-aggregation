package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;

public class NordeaDateUtilTest {

    private static final SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Test
    public void testGetTransferDateForIntraBankTransfer_SameDate_IfBusinessDate() throws Exception {
        Date date = formatter.parse("2020-01-29T00:00:00Z");
        assertEquals(NordeaDateUtil.getTransferDateForIntraBankTransfer(date), date);
    }

    @Test
    public void testGetTransferDateForIntraBankTransfer_SameDate_IfSaturday() throws Exception {
        Date date = formatter.parse("2020-01-25T00:00:00Z");
        assertEquals(NordeaDateUtil.getTransferDateForIntraBankTransfer(date), date);
    }

    @Test
    public void testGetTransferDateForIntraBankTransfer_SameDate_IfHoliday() throws Exception {
        Date date = formatter.parse("2020-06-20T00:00:00Z");
        assertEquals(NordeaDateUtil.getTransferDateForIntraBankTransfer(date), date);
    }

    @Test
    public void testGetTransferDateForIntraBankTransfer_Now_IfNull() {
        String instantExpected = "2020-01-23T10:15:30Z";
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForIntraBankTransfer(null).toInstant());
    }

    @Test
    public void testGetTransferDateForBgPg_SameBusinessDate_IfBusinessDateBeforeCutOff()
            throws Exception {
        String instantExpected = "2020-01-23T09:15:30Z";
        Date date = formatter.parse(instantExpected);
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForBgPg(date).toInstant());
    }

    @Test
    public void testGetTransferDateForBgPg_SameBusinessDate_IfBusinessDateAfterCutOff()
            throws Exception {
        String instantExpected = "2020-01-23T13:15:30Z";
        Date date = formatter.parse(instantExpected);
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForInterBankTransfer(date).toInstant());
    }

    @Test
    public void testGetTransferDateForBgPg_SameBusinessDate_IfNullBeforeCutOff() {
        String instantExpected = "2020-01-23T08:15:30Z";
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForBgPg(null).toInstant());
    }

    @Test
    public void testGetTransferDateForBgPg_NextBusinessDate_IfNullAfterCutOff() {
        String clock = "2020-01-23T09:15:30Z";
        String nextDay = "2020-01-24T09:15:30Z";
        NordeaDateUtil.setClockForTesting(Clock.fixed(Instant.parse(clock), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(nextDay), NordeaDateUtil.getTransferDateForBgPg(null).toInstant());
    }

    @Test
    public void
            testGetTransferDateForInterBankTransfer_SameBusinessDate_IfBusinessDateBeforeCutOff()
                    throws Exception {
        String instantExpected = "2020-01-23T09:15:30Z";
        Date date = formatter.parse(instantExpected);
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForInterBankTransfer(date).toInstant());
    }

    @Test
    public void testGetTransferDateForInterBankTransfer_SameBusinessDate_IfBusinessDateAfterCutOff()
            throws Exception {
        String instantExpected = "2020-01-23T13:15:30Z";
        Date date = formatter.parse(instantExpected);
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForInterBankTransfer(date).toInstant());
    }

    @Test
    public void testGetTransferDateForInterBankTransfer_SameBusinessDate_IfNullBeforeCutOff() {
        String instantExpected = "2020-01-23T08:15:30Z";
        NordeaDateUtil.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(instantExpected),
                NordeaDateUtil.getTransferDateForInterBankTransfer(null).toInstant());
    }

    @Test
    public void testGetTransferDateForInterBankTransfer_NextBusinessDate_IfNullAfterCutOff() {
        String clock = "2020-01-23T13:15:30Z";
        String nextDay = "2020-01-24T13:15:30Z";
        NordeaDateUtil.setClockForTesting(Clock.fixed(Instant.parse(clock), ZoneId.of("CET")));
        assertEquals(
                Instant.parse(nextDay),
                NordeaDateUtil.getTransferDateForInterBankTransfer(null).toInstant());
    }
}
