package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ClickAndBuyTest {

    @Test
    public void testNamePatterns() throws Exception {
        ClickAndBuy clickAndBuy = new ClickAndBuy();

        // Non matched names
        assertThat(clickAndBuy.matches("Should not ")).isFalse();

        // Matched name
        assertThat(clickAndBuy.matches("ClickandBuy International")).isTrue();
        assertThat(clickAndBuy.matches("ClickandBuy International Ltd")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        ClickAndBuy clickAndBuy = new ClickAndBuy();

        // Non matched descriptions
        assertThat(clickAndBuy.getDescription("123 description 345")).isNull();
        assertThat(clickAndBuy.getDescription("123 123 345")).isNull();

        // Matched descriptions ClickandBuy and then name of merchant
        assertThat(clickAndBuy.getDescription("ClickandBuy - Tinder")).isEqualTo("Tinder");
        assertThat(clickAndBuy.getDescription("ClickandBuy - Tinder Premium")).isEqualTo("Tinder Premium");
        assertThat(clickAndBuy.getDescription("ClickandBuy - Go-Daddy")).isEqualTo("Go-Daddy");
    }
}
