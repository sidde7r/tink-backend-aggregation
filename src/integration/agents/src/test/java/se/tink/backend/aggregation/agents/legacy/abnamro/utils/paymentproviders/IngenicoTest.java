package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IngenicoTest {

    @Test
    public void testNamePatterns() throws Exception {
        Ingenico ingenico = new Ingenico();

        // Non matched names
        assertThat(ingenico.matches("Should not match")).isFalse();

        // Matched name
        assertThat(ingenico.matches("Ingenico")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        Ingenico ingenico = new Ingenico();

        // Non matched descriptions
        assertThat(ingenico.getDescription("123 description 345")).isNull();
        assertThat(ingenico.getDescription("123 123 345")).isNull();

        // Matched descriptions
        assertThat(
                        ingenico.getDescription(
                                "3293941111 0000001 111100121 111111111 Van Uffeleode B.V."))
                .isEqualTo("Van Uffeleode B.V.");

        assertThat(
                        ingenico.getDescription(
                                "1882811111 0000001 236211111 11111111-20160418070449 Eurocamp"))
                .isEqualTo("Eurocamp");

        assertThat(ingenico.getDescription("642767830 17080204373646 N.V Staples Netherlands"))
                .isEqualTo("N.V Staples Netherlands");
    }
}
