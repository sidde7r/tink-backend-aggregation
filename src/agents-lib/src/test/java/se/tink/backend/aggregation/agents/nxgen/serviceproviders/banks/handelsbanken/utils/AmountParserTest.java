package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils;

import java.util.Optional;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class AmountParserTest {

    private String input;
    private Optional<Double> parsedAmount;

    @Test
    public void nullInput() {
        input = null;

        parseNavAmount();

        assertNavAmountNotPresent();
    }

    @Test
    public void emptyInput() {
        input = "";

        parseNavAmount();

        assertNavAmountNotPresent();
    }

    @Test
    public void nonMatching() {
        input = "This amount 200 is non-matching";

        parseNavAmount();

        assertNavAmountNotPresent();
    }

    @Test
    public void liveInput() {
        input = "200,13 SEK (2018-01-22)";

        parseNavAmount();

        assertNavAmountPresent();
    }

    @Test
    public void amountWithDotDecimalOperator() {
        input = "200.13 SEK (2018-01-22)";

        parseNavAmount();

        assertNavAmountPresent();
    }

    @Test
    public void otherAmountsIgnored() {
        input = "You win 200,13 SEK out of 300,12 EUR";

        parseNavAmount();

        assertNavAmountPresent();
    }

    private void assertNavAmountNotPresent() {
        assertThat(parsedAmount, notNullValue());
        assertThat(parsedAmount.isPresent(), is(false));
    }

    private void assertNavAmountPresent() {
        assertThat(parsedAmount, notNullValue());
        assertThat(parsedAmount.isPresent(), is(true));
        assertThat(parsedAmount.get(), is(200.13));
    }

    private void parseNavAmount() {
        parsedAmount = new AmountParser(input).parse();
    }
}
