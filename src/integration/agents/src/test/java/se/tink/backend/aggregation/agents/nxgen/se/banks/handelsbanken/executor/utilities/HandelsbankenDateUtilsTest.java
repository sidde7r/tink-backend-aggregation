package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;

public class HandelsbankenDateUtilsTest {

    @Test
    public void testPaymentDateIsNotChanged_when_ExplicitlySet() {
        // given
        HandelsbankenDateUtils handelsbankenDateUtils = new HandelsbankenDateUtils();
        LocalDate localDate = LocalDate.of(2020, 5, 29);
        final Date executionDate = Date.from(localDate.atStartOfDay(ZoneId.of("CET")).toInstant());

        // when
        final String paymentDate = handelsbankenDateUtils.getTransferDateForBgPg(executionDate);

        // then
        assertEquals(paymentDate, localDate.toString());
    }

    @Test
    public void testGetTransferDateForBgPg_SameBusinessDate_IfBusinessDateBeforeCutOff()
            throws Exception {
        String instantExpected = "2020-01-23T09:15:30Z";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = formatter.parse(instantExpected);
        HandelsbankenDateUtils.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        HandelsbankenDateUtils handelsbankenDateUtils = new HandelsbankenDateUtils();
        assertEquals("2020-01-23", handelsbankenDateUtils.getTransferDateForBgPg(date));
    }

    @Test
    public void
            testGetTransferDateForExternalA2A_DifferentBusinessDate_IfBusinessDateAfterCutOff() {
        String instantExpected = "2020-01-23T19:15:30Z";
        HandelsbankenDateUtils.setClockForTesting(
                Clock.fixed(Instant.parse(instantExpected), ZoneId.of("CET")));
        HandelsbankenDateUtils handelsbankenDateUtils = new HandelsbankenDateUtils();
        assertEquals("2020-01-24", handelsbankenDateUtils.getTransferDateForExternalTransfer(null));
    }
}
