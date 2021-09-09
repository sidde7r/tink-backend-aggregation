package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionDateMapperTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String INSTANT_WITH_UKOB_DEFAULT_TIME = "2002-04-11T00:00:00.000Z";
    private static final String INSTANT = "2002-04-11T12:15:30.345Z";
    private static final String INSTANT_WITH_UKOB_DEFAULT_TIME_IN_UK_SUMMER_TIME =
            "2002-04-11T00:00:00.000+01:00";
    private static final String INSTANT_IN_UK_SUMMER_TIME = "2002-04-11T00:15:30.345+01:00";

    @Test
    public void shouldMapDefaultTransactionTimeAsNullInstant() {
        // given
        Instant givenInstant = Instant.from(FORMATTER.parse(INSTANT_WITH_UKOB_DEFAULT_TIME));

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
    public void shouldMapTransactionTimeAsProperInstant() {
        // given
        Instant givenInstant = Instant.from(FORMATTER.parse(INSTANT));

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-11"))
                        .setInstant(Instant.parse("2002-04-11T12:15:30.345Z"))
                        .toString(),
                result.toString());
    }

    @Test
    public void shouldMapDefaultTransactionTimeAsProperInstantWithSummerUkTime() {
        // given
        Instant givenInstant =
                Instant.from(FORMATTER.parse(INSTANT_WITH_UKOB_DEFAULT_TIME_IN_UK_SUMMER_TIME));

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-10"))
                        .setInstant(null)
                        .toString(),
                result.toString());
    }

    @Test
    public void shouldMapTransactionTimeAsProperInstantWithSummerUkTime() {
        // given
        Instant givenInstant = Instant.from(FORMATTER.parse(INSTANT_IN_UK_SUMMER_TIME));

        // when
        AvailableDateInformation result =
                TransactionDateMapper.prepareTransactionDate(givenInstant);

        // then
        Assert.assertEquals(
                new AvailableDateInformation()
                        .setDate(LocalDate.parse("2002-04-10"))
                        .setInstant(Instant.parse("2002-04-10T23:15:30.345Z"))
                        .toString(),
                result.toString());
    }
}
