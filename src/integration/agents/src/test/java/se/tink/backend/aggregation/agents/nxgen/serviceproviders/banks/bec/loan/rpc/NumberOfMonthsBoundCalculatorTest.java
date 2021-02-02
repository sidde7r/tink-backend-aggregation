package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class NumberOfMonthsBoundCalculatorTest {

    private final NumberOfMonthsBoundCalculator calc = new NumberOfMonthsBoundCalculator();

    private Object[] maturityParams() {
        return new Object[] {
            new Object[] {"26 years, 9 months", 321},
            new Object[] {"26 years", 312},
            new Object[] {"9 months", 9},
            new Object[] {"26 år, 9 måneder", 321},
            new Object[] {"26 år", 312},
            new Object[] {"9 måneder", 9}
        };
    }

    @Test
    @Parameters(method = "maturityParams")
    public void shouldSucceed(final String input, final int expectedResult) {
        // given

        // when
        int result = calc.calculate(input);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void shouldReturnZeroWhenInputContainsUnknownMarks() {
        // given
        String input = "26 YR, 9 MTH";
        int expectedResult = 0;

        // when
        int result = calc.calculate(input);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void shouldReturnZeroWhenInputIsEmpty() {
        // given
        String input = "";
        int expectedResult = 0;

        // when
        int result = calc.calculate(input);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void shouldReturnZeroWhenInputMissingSeparator() {
        // given
        String input = "26 years 9 months";
        int expectedResult = 0;

        // when
        int result = calc.calculate(input);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }
}
