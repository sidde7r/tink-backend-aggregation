package se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GlobalCollectTest {

    @Test
    public void testNamePatterns() throws Exception {
        GlobalCollect globalCollect = new GlobalCollect();

        // Non matched names
        assertThat(globalCollect.matches("Should not match")).isFalse();

        // Matched name
        assertThat(globalCollect.matches("GlobalCollect")).isTrue();
    }

    @Test
    public void testNormalPatterns() throws Exception {
        GlobalCollect globalCollect = new GlobalCollect();

        // Non matched descriptions
        assertThat(globalCollect.getDescription("123 description 345")).isNull();
        assertThat(globalCollect.getDescription("123 123 345")).isNull();

        // Matched descriptions numbers and description in the end
        assertThat(globalCollect.getDescription("111112945111 1110001159792111 1211121212122 Steampowered"))
                .isEqualTo("Steampowered");

        assertThat(globalCollect.getDescription("0020001149855175 0020001149855175 CY-451237296 CFD-trading-platform"))
                .isEqualTo("CFD-trading-platform");

        assertThat(globalCollect.getDescription("267645167669 0020001133185803 FastSpring"))
                .isEqualTo("FastSpring");
    }

    @Test
    public void testKlmPatterns() throws Exception {
        GlobalCollect globalCollect = new GlobalCollect();

        assertThat(globalCollect.getDescription("267646117819 0000001151570155 KLM*Ref AA44AA KLM"))
                .isEqualTo("KLM");

        assertThat(globalCollect.getDescription("267645167669 0020001133185803 KLM*Ref 77GVXW KLM Flight"))
                .isEqualTo("KLM Flight");
    }
}
