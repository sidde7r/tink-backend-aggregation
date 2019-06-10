package se.tink.libraries.cryptography;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class HMACUtilsTest {

    @Test
    public void shouldGenerateHMAC() {
        String hmac = HMACUtils.calculateMac("data", "secretKey");

        Assertions.assertThat(hmac).isEqualTo("stxddIQ4rlwEz+HiddFYIPAKdi8=");
    }
}
