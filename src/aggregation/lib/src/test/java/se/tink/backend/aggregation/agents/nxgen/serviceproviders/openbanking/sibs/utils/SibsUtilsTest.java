package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SibsUtilsTest {

    private static final String EXPECTED_SIGNING_STRING =
            "digest: SHA-256=dummyDigest\n"
                    + "tpp-transaction-id: dummyTransactionId\n"
                    + "tpp-request-id: dummyRequestId\n"
                    + "date: dummyDate";

    private static final String EXPECTED_DIGEST = "LWFzxWAWcmfw7V0AsX2Tx4pNg3s0WCVJ7w5bO4zwJhs=";

    @Test
    public void shouldReturnSigningString() {
        String signature =
                SibsUtils.getSigningString(
                        "dummyDigest", "dummyTransactionId", "dummyRequestId", "dummyDate");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_SIGNING_STRING);
    }

    @Test
    public void shouldCalculateDigest() {
        String signature = SibsUtils.getDigest("dummyBody");

        Assertions.assertThat(signature).isEqualTo(EXPECTED_DIGEST);
    }
}
