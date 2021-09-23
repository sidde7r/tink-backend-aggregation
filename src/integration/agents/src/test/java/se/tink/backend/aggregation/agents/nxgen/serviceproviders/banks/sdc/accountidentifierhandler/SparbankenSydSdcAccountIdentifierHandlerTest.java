package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.google.common.collect.ImmutableList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@RunWith(JUnitParamsRunner.class)
public class SparbankenSydSdcAccountIdentifierHandlerTest {

    private static final String EXCEPTION_MSG = "Given account number has illegal format: ";

    private final SparbankenSydSdcAccountIdentifierHandler accountIdentifierHandler =
            new SparbankenSydSdcAccountIdentifierHandler();

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
        String result = accountIdentifierHandler.convertToIban(givenAccountNumber);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    public void convertToIbanShouldThrowExceptionWhenAccountNoContainsMoreThanOneDot() {
        // given
        String accountNo = "9570.10805.55";

        // when
        Throwable t = catchThrowable(() -> accountIdentifierHandler.convertToIban(accountNo));

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
        Throwable t = catchThrowable(() -> accountIdentifierHandler.convertToIban(accountNo));

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
        Throwable t = catchThrowable(() -> accountIdentifierHandler.convertToIban(accountNo));

        // then
        assertThat(t)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEPTION_MSG + accountNo);
    }

    @Test
    @Parameters({
        "5250971883, 95700000005250971883, SE1695700000005250971883, 5250971883",
        "9570.1080555, 95700000000001080555, SE4395700000000001080555, 95701080555",
        "9570.39506274, 95700000000039506274, SE7395700000000039506274, 957039506274",
        "9570.5251229422, 95700000005251229422, SE0595700000005251229422, 95705251229422",
        "95700033028044, 95700000000033028044, SE4095700000000033028044, 95700033028044"
    })
    public void shouldReturnCorrectBbanIbanAndSwedishIdentifier(
            final String givenAccountNumber,
            final String expectedBban,
            final String expectedIban,
            final String expectedSwedishIdentifier) {
        // given

        // when
        List<AccountIdentifier> identifiers =
                accountIdentifierHandler.getIdentifiers(givenAccountNumber);

        // then
        assertThat(identifiers)
                .containsAll(
                        ImmutableList.of(
                                new BbanIdentifier(expectedBban),
                                new IbanIdentifier(expectedIban),
                                new SwedishIdentifier(expectedSwedishIdentifier)));
    }
}
