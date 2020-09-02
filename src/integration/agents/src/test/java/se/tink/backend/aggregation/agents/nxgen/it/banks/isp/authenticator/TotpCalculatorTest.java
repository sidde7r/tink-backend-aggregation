package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TotpCalculatorTest {
    private static final String MASK = "c8efc25c440ab641132265749b539ebdbbda76c1";
    private static final String SEED = "13579";
    private static final long TIME = 0x05df38fd0;
    private static final String EXPECTED = "725709";

    @Test
    public void shouldReturnCorrectOtp() {
        String generatedOtp = TotpCalculator.calculateTOTP(6, MASK, SEED, TIME);
        Assertions.assertThat(generatedOtp).isEqualTo(EXPECTED);
    }
}
