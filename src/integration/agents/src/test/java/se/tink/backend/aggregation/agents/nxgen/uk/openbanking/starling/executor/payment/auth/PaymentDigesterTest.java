package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc.InstructLocalPaymentRequest;

public class PaymentDigesterTest {
    @Test
    public void testPaymentDigest() {
        PaymentDigester digester = new PaymentDigester();
        InstructLocalPaymentRequest empty = InstructLocalPaymentRequest.builder().build();
        Assertions.assertThat(digester.digest(empty))
                .isEqualTo(
                        "OaRDp51VtKbAmFmb5Zhab4KVgA19UszY/1b3MLDeGcuoSt4vUVh7rST+LskN5Xp46OSV/bBeKS3YG4kTYgc14w==");
    }
}
