package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AmountEntityTest {

    @Test
    public void testAmountShouldHaveTwoDecimals() {
        // Arrange
        AmountEntity amount1 = new AmountEntity(ExactCurrencyAmount.of("1", "GBP"));
        AmountEntity amount2 = new AmountEntity(ExactCurrencyAmount.of("1.00", "GBP"));
        AmountEntity amount3 = new AmountEntity(ExactCurrencyAmount.of("1.01", "GBP"));
        AmountEntity amount4 = new AmountEntity(ExactCurrencyAmount.of("100000", "GBP"));
        AmountEntity amount5 = new AmountEntity(ExactCurrencyAmount.of("1.0123", "GBP"));
        AmountEntity amount6 = new AmountEntity(ExactCurrencyAmount.of("1.09999", "GBP"));

        // Act & Assert
        Assert.assertEquals(amount1.getAmount(), "1.00");
        Assert.assertEquals(amount2.getAmount(), "1.00");
        Assert.assertEquals(amount3.getAmount(), "1.01");
        Assert.assertEquals(amount4.getAmount(), "100000.00");
        Assert.assertEquals(amount5.getAmount(), "1.01");
        Assert.assertEquals(amount6.getAmount(), "1.10");
    }

    @Test
    public void testAmountShoulNotHaveGroupSeparator() {
        // Arrange
        AmountEntity amount = new AmountEntity(ExactCurrencyAmount.of("100000", "GBP"));

        // Act & Assert
        Assert.assertEquals(amount.getAmount(), "100000.00");
    }
}
