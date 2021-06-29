package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AmountEntityTest {

    @Test
    public void shouldParseAmountToTwoDecimalPlacesAfterComaWhenNoneWasProvided() {
        // given
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.of(1, "EUR");

        // when
        final AmountEntity amountEntity = AmountEntity.withAmount(exactCurrencyAmount);

        // then
        assertEquals("1.00", amountEntity.getAmount());
    }

    @Test
    public void shouldParseAmountToTwoDecimalPlacesAfterComaWhenOneWasProvided() {
        // given
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.of(1.0, "EUR");

        // when
        final AmountEntity amountEntity = AmountEntity.withAmount(exactCurrencyAmount);

        // then
        assertEquals("1.00", amountEntity.getAmount());
    }

    @Test
    public void shouldParseAmountToTwoDecimalPlacesAfterComaWhenTwoWereProvided() {
        // given
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.of(1.09, "EUR");

        // when
        final AmountEntity amountEntity = AmountEntity.withAmount(exactCurrencyAmount);

        // then
        assertEquals("1.09", amountEntity.getAmount());
    }
}
