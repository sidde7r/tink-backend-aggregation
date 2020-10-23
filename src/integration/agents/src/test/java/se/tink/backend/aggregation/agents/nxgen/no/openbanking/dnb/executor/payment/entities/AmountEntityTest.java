package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.amount.Amount;

public class AmountEntityTest {

    @Test
    public void toTinkAmount() {
        // given
        double amount = 1234.5678;
        double expectedAmount = 1234.56;
        String currency = "EUR";
        AmountEntity entity = new AmountEntity(amount, currency);

        // when
        Amount result = entity.toTinkAmount();

        // then
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getValue()).isEqualTo(expectedAmount);
        assertThat(result).isEqualTo(Amount.inEUR(expectedAmount));
    }
}
