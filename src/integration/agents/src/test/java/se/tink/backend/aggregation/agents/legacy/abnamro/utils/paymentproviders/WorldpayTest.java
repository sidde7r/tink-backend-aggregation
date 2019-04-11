package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WorldpayTest {

    @Test
    public void testNamePatterns() throws Exception {
        WorldPay worldPay = new WorldPay();

        // Non matched names
        assertThat(worldPay.matches("Should not match")).isFalse();

        // Matched name
        assertThat(worldPay.matches("Worldpay AP LTD")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        WorldPay worldPay = new WorldPay();

        // Non matched descriptions
        assertThat(worldPay.getDescription("123 description 345")).isNull();
        assertThat(worldPay.getDescription("123 123 345")).isNull();

        // Could probably be matched but we haven't seen these
        assertThat(worldPay.getDescription("USOSDOKKS2323000000Unibet")).isNull();
        assertThat(worldPay.getDescription("USOSDOKKS2323 000Unibet")).isNull();

        // Matched descriptions
        assertThat(worldPay.getDescription("USOSDOKKS2323 000111Unibet")).isEqualTo("Unibet");
    }
}
