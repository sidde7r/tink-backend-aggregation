package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MultisafePayTest {

    @Test
    public void testNamePatterns() throws Exception {
        MultisafePay multisafePay = new MultisafePay();

        // Non matched names
        assertThat(multisafePay.matches("Should not match")).isFalse();

        // Matched name
        assertThat(multisafePay.matches("MultiSafepay")).isTrue();
    }
}
