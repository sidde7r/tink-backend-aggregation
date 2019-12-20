package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import org.junit.Assert;
import org.junit.Test;

public class BpiIbanCalculatorTest {

    @Test
    public void shouldCalculateIbanForEuroCurrency() {
        // given
        final String expectedIban = "PT50 001000001962511000111";
        BpiIbanCalculator objectUnderTest = new BpiIbanCalculator("1962511", "000", "001");
        // when
        String result = objectUnderTest.calculateIban();
        // then
        Assert.assertEquals(expectedIban, result);
    }

    @Test
    public void shouldCalculateIbanForNotEuroCurrency() {
        // given
        final String expectedIban = "PT50 001099991962511060131";
        BpiIbanCalculator objectUnderTest = new BpiIbanCalculator("1962511", "306", "001");
        // when
        String result = objectUnderTest.calculateIban();
        // then
        Assert.assertEquals(expectedIban, result);
    }
}
