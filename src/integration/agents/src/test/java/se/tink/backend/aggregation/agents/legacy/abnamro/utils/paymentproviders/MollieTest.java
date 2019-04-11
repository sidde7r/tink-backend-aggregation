package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MollieTest {

    @Test
    public void testNamePatterns() throws Exception {
        Mollie mollie = new Mollie();

        // Non matched names
        assertThat(mollie.matches("Should not match")).isFalse();

        // Matched name
        assertThat(mollie.matches("Stg Mollie Payments")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        Mollie mollie = new Mollie();

        // Non matched descriptions
        assertThat(mollie.getDescription("123 description 345")).isNull();
        assertThat(mollie.getDescription("123 123 345")).isNull();

        // Don't match anything
    }
}
