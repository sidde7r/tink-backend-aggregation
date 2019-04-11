package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SisowTest {

    @Test
    public void testNamePatterns() throws Exception {
        Sisow sisow = new Sisow();

        // Non matched names
        assertThat(sisow.matches("Should not match")).isFalse();

        // Matched name
        assertThat(sisow.matches("Stichting Sisow")).isTrue();
    }
}
