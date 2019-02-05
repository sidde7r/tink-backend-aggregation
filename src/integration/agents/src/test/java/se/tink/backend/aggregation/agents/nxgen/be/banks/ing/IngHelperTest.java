package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class IngHelperTest {

    @Test
    public void testAmountParsing() {
        String amount1 = "+000000000000123452";
        String amount2 = "+000000000000000002";
        String amount3 = "-000000000000300022";

        Double parsedAmount1 = IngHelper.parseAmountStringToDouble(amount1);
        Double parsedAmount2 = IngHelper.parseAmountStringToDouble(amount2);
        Double parsedAmount3 = IngHelper.parseAmountStringToDouble(amount3);

        Assertions.assertThat(parsedAmount1).isEqualTo(123.45);
        Assertions.assertThat(parsedAmount2).isEqualTo(0.00);
        Assertions.assertThat(parsedAmount3).isEqualTo(-300.02);
    }
}
