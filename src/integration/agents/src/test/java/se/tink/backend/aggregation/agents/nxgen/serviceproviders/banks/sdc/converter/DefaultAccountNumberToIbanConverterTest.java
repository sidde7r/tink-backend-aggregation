package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DefaultAccountNumberToIbanConverterTest {

    private static final String RAW_ACCOUNT_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private static final String BANK_ID_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private static final String ACCOUNT_NUMBER_ILLEGAL_FORMAT_EXCEPTION_MSG =
            "Given account number has illegal format: ";

    private AccountNumberToIbanConverter dkConverter =
            DefaultAccountNumberToIbanConverter.DK_CONVERTER;
    private AccountNumberToIbanConverter foConverter =
            DefaultAccountNumberToIbanConverter.FO_CONVERTER;
    private AccountNumberToIbanConverter noConverter =
            DefaultAccountNumberToIbanConverter.NO_CONVERTER;

    @Test
    @Parameters({
        "6.539893, DK8300060000539893",
        "3.2367306, DK4100030002367306",
        "2.9831001158, DK0500029831001158",
        "46.539893, DK2800460000539893",
        "53.2367306, DK4500530002367306",
        "62.9831001158, DK6800629831001158",
        "436.539893, DK9804360000539893",
        "233.2367306, DK4002330002367306",
        "452.9831001158, DK4104529831001158",
        "6734.539893, DK4767340000539893",
        "3853.2367306, DK5838530002367306",
        "6472.9831001158, DK5764729831001158",
        "53429988776655, DK9153429988776655"
    })
    public void convertToIbanUsingDKConverter(
            final String givenAccountNumber, final String expectedIban) {
        // given

        // when
        String result = dkConverter.convertToIban(givenAccountNumber);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    @Parameters({
        "6.539893, FO5300060000539893",
        "3.2367306, FO1100030002367306",
        "2.9831001158, FO7200029831001158",
        "46.539893, FO9500460000539893",
        "53.2367306, FO1500530002367306",
        "62.9831001158, FO3800629831001158",
        "436.539893, FO6804360000539893",
        "233.2367306, FO1002330002367306",
        "452.9831001158, FO1104529831001158",
        "6734.539893, FO1767340000539893",
        "3853.2367306, FO2838530002367306",
        "6472.9831001158, FO2764729831001158",
        "53429988776655, FO6153429988776655"
    })
    public void convertToIbanUsingFoConverter(
            final String givenAccountNumber, final String expectedIban) {
        // given

        // when
        String result = foConverter.convertToIban(givenAccountNumber);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    @Parameters({
        "6.539893, NO6900060539893",
        "3.2367306, NO8000032367306",
        "46.539893, NO5100460539893",
        "53.2367306, NO0900532367306",
        "436.539893, NO2104360539893",
        "233.2367306, NO2502332367306",
        "6734.539893, NO2967340539893",
        "3853.2367306, NO4538532367306",
        "53429988776, NO1053429988776"
    })
    public void convertToIbanUsingNoConverter(
            final String givenAccountNumber, final String expectedIban) {
        // given

        // when
        String result = noConverter.convertToIban(givenAccountNumber);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoContainsMoreThanOneDot() {
        // given
        String accountNo = "436.539.893";

        // when
        Throwable t = catchThrowable(() -> dkConverter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RAW_ACCOUNT_ILLEGAL_FORMAT_EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoContainsNotDigitsOnly() {
        // given
        String accountNo = "436.539P893";

        // when
        Throwable t = catchThrowable(() -> dkConverter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ACCOUNT_NUMBER_ILLEGAL_FORMAT_EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenBankIdContainsNotDigitsOnly() {
        // given
        String accountNo = "43P6.539893";

        // when
        Throwable t = catchThrowable(() -> dkConverter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BANK_ID_ILLEGAL_FORMAT_EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoIsTooLong() {
        // given
        String accountNo = "436.12345678912";

        // when
        Throwable t = catchThrowable(() -> dkConverter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ACCOUNT_NUMBER_ILLEGAL_FORMAT_EXCEPTION_MSG + accountNo);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenBankIdIsTooLong() {
        // given
        String accountNo = "12345.123456";

        // when
        Throwable t = catchThrowable(() -> dkConverter.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BANK_ID_ILLEGAL_FORMAT_EXCEPTION_MSG + accountNo);
    }
}
