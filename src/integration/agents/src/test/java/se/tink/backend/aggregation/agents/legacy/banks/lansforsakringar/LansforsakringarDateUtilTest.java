package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;

public class LansforsakringarDateUtilTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final String BUSINESS_DAY_BEFORE_CUT_OFF_TIME = "2020-03-19T09:00:00.00Z";

    private Clock fixedClock(String moment) {
        Instant instant = Instant.parse(moment);
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    @Test
    public void testGetCurrentOrNextBusinessDate_isMovedToNextBusinessDate() {
        // when
        LocalDate localDate = LocalDate.of(2020, 3, 22);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(localDate);

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

    @Test
    public void testPaymentDateIsDefaulted_when_notSetAndBeforeCutOffTime() {
        // given
        LansforsakringarDateUtil.setClock(fixedClock(BUSINESS_DAY_BEFORE_CUT_OFF_TIME));

        // when
        final long paymentDate = LansforsakringarDateUtil.getNextPossiblePaymentDateForBgPg(null);

        // then
        final long now = Date.from(Instant.parse(BUSINESS_DAY_BEFORE_CUT_OFF_TIME)).getTime();
        assertEquals(paymentDate, now);
    }

    @Test
    public void testPaymentDateIsMovedToNextBusinessDay_when_notSetAndPastCutOffTime() {
        // given
        // Thursday after cutoff time
        LansforsakringarDateUtil.setClock(fixedClock("2020-03-19T15:00:00.00Z"));

        // when
        final long paymentDate = LansforsakringarDateUtil.getNextPossiblePaymentDateForBgPg(null);

        // then
        final long nextPossibleBusinessDay =
                Date.from(Instant.parse("2020-03-20T15:00:00.00Z")).getTime();
        assertEquals(paymentDate, nextPossibleBusinessDay);
    }

    @Test
    public void testPaymentDateIsNotChanged_when_ExplicitlySet() {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2020-03-19T03:30:00.00Z"));

        // when
        final Date executionDate = Date.from(Instant.parse("2020-03-22T03:30:00.00Z"));
        final long paymentDate =
                LansforsakringarDateUtil.getNextPossiblePaymentDateForBgPg(executionDate);

        // then
        assertEquals(paymentDate, executionDate.getTime());
    }
}
