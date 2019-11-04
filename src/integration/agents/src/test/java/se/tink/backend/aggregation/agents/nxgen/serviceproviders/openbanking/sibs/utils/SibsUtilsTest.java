package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SibsUtilsTest {

    private static final String EXPECTED_DIGEST = "LWFzxWAWcmfw7V0AsX2Tx4pNg3s0WCVJ7w5bO4zwJhs=";

    @Test
    public void shouldCalculateDigest() {
        String signature = SibsUtils.getDigest("dummyBody");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_DIGEST);
    }
}
