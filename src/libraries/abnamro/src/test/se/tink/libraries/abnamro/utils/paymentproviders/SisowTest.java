package se.tink.libraries.abnamro.utils.paymentproviders;

import org.junit.Test;
import se.tink.libraries.abnamro.utils.paymentproviders.Sisow;
import static org.assertj.core.api.Assertions.assertThat;

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
