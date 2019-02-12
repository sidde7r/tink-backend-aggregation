package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BuckarooTest {

    @Test
    public void testNamePatterns() throws Exception {
        Buckaroo buckaroo = new Buckaroo();

        // Non matched names
        assertThat(buckaroo.matches("Should not match")).isFalse();

        // Matched name
        assertThat(buckaroo.matches("Stichting Derdengelden Buckaroo")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        Buckaroo buckaroo = new Buckaroo();

        // Non matched descriptions
        assertThat(buckaroo.getDescription("123 description 345")).isNull();
        assertThat(buckaroo.getDescription("123 123 345")).isNull();

        // Don't match anything for this provider so far
    }
}
