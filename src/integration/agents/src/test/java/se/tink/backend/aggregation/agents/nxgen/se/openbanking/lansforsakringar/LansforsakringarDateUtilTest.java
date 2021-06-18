package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Test;

public class LansforsakringarDateUtilTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    @Test
    public void testGetCurrentOrNextBusinessDate_isMovedToNextBusinessDate() {
        // when
        LocalDate localDate = LocalDate.of(2020, 3, 22);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(localDate);

        // then
        assertEquals(currentOrNextBusinessDate, LocalDate.of(2020, 3, 22));
    }

    @Test
    public void testNullInput_willGetClosestBusinessDate() {
        Instant instant = Instant.parse("2020-03-22T03:30:00.00Z");
        LansforsakringarDateUtil.setClock(Clock.fixed(instant, DEFAULT_ZONE_ID));
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(null);

        // then
        assertEquals(currentOrNextBusinessDate, LocalDate.of(2020, 3, 23));
    }

    @Test
    public void testGetCurrentOrNextBusinessDate_isNotMovedToNextBusinessDate() {
        // when
        LocalDate from = LocalDate.of(2020, 3, 23);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(from);

        // then
        assertEquals(currentOrNextBusinessDate, LocalDate.of(2020, 3, 23));
    }
}
