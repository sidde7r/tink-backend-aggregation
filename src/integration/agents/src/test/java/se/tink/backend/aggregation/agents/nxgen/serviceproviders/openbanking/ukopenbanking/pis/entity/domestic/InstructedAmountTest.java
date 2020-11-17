package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InstructedAmountTest {

    @Test
    public void testAmountShouldHaveTwoDecimals() {
        // Arrange
        InstructedAmount amount1 = new InstructedAmount(ExactCurrencyAmount.of("1", "GBP"));
        InstructedAmount amount2 = new InstructedAmount(ExactCurrencyAmount.of("1.00", "GBP"));
        InstructedAmount amount3 = new InstructedAmount(ExactCurrencyAmount.of("1.01", "GBP"));
        InstructedAmount amount4 = new InstructedAmount(ExactCurrencyAmount.of("100000", "GBP"));
        InstructedAmount amount5 = new InstructedAmount(ExactCurrencyAmount.of("1.0123", "GBP"));
        InstructedAmount amount6 = new InstructedAmount(ExactCurrencyAmount.of("1.09999", "GBP"));

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
        InstructedAmount amount = new InstructedAmount(ExactCurrencyAmount.of("100000", "GBP"));

        // Act & Assert
        Assert.assertEquals(amount.getAmount(), "100000.00");
    }
}
