package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Test;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

public class LansforsakringarDateUtilTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    @Test
    public void testGetCurrentOrNextBusinessDate_isMovedToNextBusinessDate() {
        // when
        LocalDate localDate = LocalDate.of(2020, 3, 22).atStartOfDay(DEFAULT_ZONE_ID).toLocalDate();
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(
                        localDate, new Creditor(new SwedishIdentifier("3300123456")));
        // then
        assertEquals(localDate, currentOrNextBusinessDate);
    }

    @Test
    public void testNullInput_willGetClosestBusinessDate_whenMondayBeforeCutOff() {
        Instant instant = Instant.parse("2020-03-23T06:30:00Z");
        Clock fixed = Clock.fixed(instant, DEFAULT_ZONE_ID);
        LansforsakringarDateUtil.setClock(fixed);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(
                        null, new Creditor(new SwedishIdentifier("3300123456")));

        // then
        assertEquals(LocalDate.now(fixed), currentOrNextBusinessDate);
    }

    @Test
    public void testNullInput_willGetClosestBusinessDate_whenMondayAfterCutOff() {
        Instant instant = Instant.parse("2020-03-23T11:30:00Z");
        Clock fixed = Clock.fixed(instant, DEFAULT_ZONE_ID);
        LansforsakringarDateUtil.setClock(fixed);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(
                        null, new Creditor(new BankGiroIdentifier("51225860")));

        // then
        assertEquals(LocalDate.now(fixed).plusDays(1L), currentOrNextBusinessDate);
    }

    @Test
    public void testGetCurrentOrNextBusinessDate_isNotMovedToNextBusinessDate() {
        // when
        Instant instant = Instant.parse("2020-03-22T09:30:00.00Z");
        Clock fixed = Clock.fixed(instant, DEFAULT_ZONE_ID);
        LansforsakringarDateUtil.setClock(fixed);
        LocalDate currentOrNextBusinessDate =
                LansforsakringarDateUtil.getCurrentOrNextBusinessDate(
                        null, new Creditor(new SwedishIdentifier("3300123456")));

        // then
        assertEquals(LocalDate.now(fixed).plusDays(1L), currentOrNextBusinessDate);
    }
}
