package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AdyenTest {

    @Test
    public void testNamePatterns() throws Exception {
        Adyen adyen = new Adyen();

        // Non matched names
        assertThat(adyen.matches("Should not match")).isFalse();
        assertThat(adyen.matches("Erik Adyen")).isFalse();

        // Matched name
        assertThat(adyen.matches("STG ADYEN")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        Adyen adyen = new Adyen();

        // Non matched descriptions
        assertThat(adyen.getDescription("123 description 345")).isNull();
        assertThat(adyen.getDescription("123 123 345")).isNull();

        // Not matched, require more characters to extract the name
        assertThat(adyen.getDescription("aa 1212")).isNull();

        // Matched descriptions (description and then numbers)
        assertThat(adyen.getDescription("Spotify 1234545345345")).isEqualTo("Spotify");
        assertThat(adyen.getDescription("Spotify 12 345 453 45 345")).isEqualTo("Spotify");

        // Matched descriptions (start with digits and description in the end)
        assertThat(adyen.getDescription("1114511873114104 1100043266 00111 8HappySocks"))
                .isEqualTo("HappySocks");
        assertThat(adyen.getDescription("1114511873114104 Some characters 00111 8HappySocks"))
                .isEqualTo("HappySocks");
    }
}
