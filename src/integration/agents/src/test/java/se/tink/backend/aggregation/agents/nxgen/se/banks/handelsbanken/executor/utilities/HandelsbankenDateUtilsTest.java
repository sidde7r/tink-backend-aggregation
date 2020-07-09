package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;

public class HandelsbankenDateUtilsTest {

    @Test
    public void testPaymentDateIsNotChanged_when_ExplicitlySet() {
        // given
        HandelsbankenDateUtils handelsbankenDateUtils = new HandelsbankenDateUtils();
        LocalDate localDate = LocalDate.of(2020, 05, 29);
        final Date executionDate = Date.from(localDate.atStartOfDay(ZoneId.of("CET")).toInstant());

        // when
        final String paymentDate = handelsbankenDateUtils.getTransferDateForBgPg(executionDate);

        // then
        assertEquals(paymentDate, localDate.toString());
    }
}
