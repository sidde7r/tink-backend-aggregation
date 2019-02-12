package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PaydotnlTest {

    @Test
    public void testNamePatterns() throws Exception {
        Paydotnl paydotnl = new Paydotnl();

        // Non matched names
        assertThat(paydotnl.matches("Should not match")).isFalse();

        // Matched name
        assertThat(paydotnl.matches("Stichting Pay.nl")).isTrue();
    }

    @Test
    public void testDescriptionPatterns() throws Exception {
        Paydotnl paydotnl = new Paydotnl();

        // Non matched descriptions
        assertThat(paydotnl.getDescription("123 description 345")).isNull();
        assertThat(paydotnl.getDescription("123 123 345")).isNull();

        // Matched descriptions
        assertThat(paydotnl.getDescription("11036511Xa7269b 1120001155071201 116 MamaLoes")).isEqualTo("MamaLoes");
        assertThat(paydotnl.getDescription("0020001147360989 1453049081-185529-010 securesafepay"))
                .isEqualTo("securesafepay");
    }
}
