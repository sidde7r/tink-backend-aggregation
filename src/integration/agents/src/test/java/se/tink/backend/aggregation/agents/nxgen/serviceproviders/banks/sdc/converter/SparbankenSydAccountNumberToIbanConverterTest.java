package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class SparbankenSydAccountNumberToIbanConverterTest {

    private static final String EXCEPTION_MSG = "Given account number has illegal format: ";

    private SparbankenSydAccountNumberToIbanConverter converter =
            new SparbankenSydAccountNumberToIbanConverter();

    @Test
    @Parameters({
        "5250971883, SE1695700000005250971883",
        "9570.1080555, SE4395700000000001080555",
        "9570.39506274, SE7395700000000039506274",
        "9570.5251229422, SE0595700000005251229422",
        "95700033028044, SE4095700000000033028044"
    })
    public void convertToIban(final String givenAccountNumber, final String expectedIban) {
        // given

        // when
        String result = converter.convertToIban(givenAccountNumber);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoContainsMoreThanOneDot() {
        // given
        String accountNo = "9570.10805.55";

        // when
        Throwable t = catchThrowable(() -> converter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoContainsNotDigitsOnly() {
        // given
        String accountNo = "9570.10805P55";

        // when
        Throwable t = catchThrowable(() -> converter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoIsTooLong() {
        // given
        String accountNo = "9570.12345678912";

        // when
        Throwable t = catchThrowable(() -> converter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEPTION_MSG + accountNo);
    }
}
