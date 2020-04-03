package se.tink.libraries.iban;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class IbanConverterTest {

    @Test
    @Parameters({
        "gb, BUKB20201555555555, GB33BUKB20201555555555",
        "GB, BUKB20201555555555, GB33BUKB20201555555555",
        "GB, bukb20201555555555, GB33bukb20201555555555",
        "DE, 512108001245126199, DE75512108001245126199",
        "FR, 30006000011234567890189, FR7630006000011234567890189",
    })
    public void getIbanTestForValidInput(
            String givenIsoCountryCode, String givenBban, String expectedIban) {
        // when
        String result = IbanConverter.getIban(givenIsoCountryCode, givenBban);

        // then
        assertThat(result).isEqualTo(expectedIban);
    }

    @Test
    public void getIbanTestForInvalidInput() {
        // given
        String givenInvalidIsoCountryCode = "abc";
        String givenBban = "30006000011234567890189";

        // when
        ThrowingCallable callable =
                () -> IbanConverter.getIban(givenInvalidIsoCountryCode, givenBban);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provided country code should be in format ISO-3166 alpha-2");
    }
}
