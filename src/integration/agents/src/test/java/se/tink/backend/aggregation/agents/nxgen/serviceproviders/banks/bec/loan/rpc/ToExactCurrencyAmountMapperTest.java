package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ToExactCurrencyAmountMapperTest {

    @Test
    public void shouldReturnNullIfCannotSeparateInputInto2Parts() {
        // given
        String input = "1.456.654,43DKK";

        // when
        ExactCurrencyAmount result = ToExactCurrencyAmountMapper.parse(input);

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnNullIfNumberIsNotParseable() {
        // given
        String input = "unparsablenumber DKK";

        // when
        ExactCurrencyAmount result = ToExactCurrencyAmountMapper.parse(input);

        // then
        assertThat(result).isNull();
    }

    @Test
    public void shouldSucceed() {
        // given
        String input = "491.000,00 DKK";

        // when
        ExactCurrencyAmount result = ToExactCurrencyAmountMapper.parse(input);

        // then
        assertThat(result).isEqualTo(ExactCurrencyAmount.inDKK(491000.0));
    }
}
