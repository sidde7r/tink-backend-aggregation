package se.tink.libraries.abnamro.utils.paymentproviders;

import org.junit.Test;
import se.tink.libraries.abnamro.utils.paymentproviders.Mollie;
import static org.assertj.core.api.Assertions.assertThat;

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
