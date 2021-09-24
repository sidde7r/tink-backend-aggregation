package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionDateMapperTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private static final String DEFAULT_INSTANT_WITH_ZULU_TIME = "2002-04-11T00:00:00.000Z";
    private static final String INSTANT_WITH_ZULU_TIME = "2002-04-11T20:13:12.666Z";
    private static final String INSTANT_WITH_UTC_1_TIME = "2002-04-11T00:13:12.666+01:00";

    @Test
    public void shouldMapDefaultTransactionTimeAsNullInstant() {
        // given
        Instant givenInstant = Instant.parse(DEFAULT_INSTANT_WITH_ZULU_TIME);

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-11"))
                        .setInstant(null)
                        .toString(),
                result.toString());
    }

    @Test
    public void shouldMapTransactionTimeAsZuluInstant() {
        // given
        Instant givenInstant = Instant.from(FORMATTER.parse(INSTANT_WITH_ZULU_TIME));

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-11"))
                        .setInstant(Instant.parse("2002-04-11T20:13:12.666Z"))
                        .toString(),
                result.toString());
    }

    @Test
    public void shouldMapTransactionTimeAsZuluInstantWhenUTC1Time() {
        // given
        Instant givenInstant = Instant.from(FORMATTER.parse(INSTANT_WITH_UTC_1_TIME));

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-10"))
                        .setInstant(Instant.parse("2002-04-10T23:13:12.666Z"))
                        .toString(),
                result.toString());
    }
}
