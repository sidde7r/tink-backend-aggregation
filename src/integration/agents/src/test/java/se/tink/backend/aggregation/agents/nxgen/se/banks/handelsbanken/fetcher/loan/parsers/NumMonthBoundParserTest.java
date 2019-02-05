package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.parsers;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NumMonthBoundParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseNull() {
        NumMonthBoundParser.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseEmpty() {
        NumMonthBoundParser.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseWithoutLengthBeforePeriod() {
        NumMonthBoundParser.parse("text");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseNonIntegerLength() {
        NumMonthBoundParser.parse("random text");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseNonTextPeriod() {
        NumMonthBoundParser.parse("5 5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableToParseWithMoreWordsThanExpectedPeriodInformation() {
        NumMonthBoundParser.parse("5 random text");
    }

    @Test
    public void correctlyParsed() {
        assertThat(NumMonthBoundParser.parse(HandelsbankenSEConstants.Fetcher.Loans.FLOATING), is(3));
        assertThat(NumMonthBoundParser.parse("2 " + HandelsbankenSEConstants.Fetcher.Loans.YEAR), is(24));
        assertThat(NumMonthBoundParser.parse("5 text"), is(5));
    }
}
