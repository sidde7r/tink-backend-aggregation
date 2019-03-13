package se.tink.libraries.amount;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AmountTest {
    @Test
    public void testTest() {
        Amount amount1 = new Amount("SEK", 100, 2);

        assertThat(amount1.getCurrency()).isEqualTo("SEK");
    }
}
