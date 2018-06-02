package se.tink.backend.aggregation.agents.banks.uk.barclays;

import org.apache.commons.codec.binary.Hex;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class KeysTest {

    @Test
    public void testKeys() {
        byte[] seed = null;
        byte[] cliAesKey = null;
        byte[] srvAesKey = null;
        byte[] cliIvKey = null;
        byte[] srvIvKey = null;
        try {
            seed = Hex.decodeHex("fdb90555fec1291826cd438897836af12b650e2dab674816c446f6f94b7e76f6".toCharArray());
            cliAesKey = Hex.decodeHex("c4ee4fc382e6eb7422c7785c90354a0a5bdffb0702caba0624fa66f246027248".toCharArray());
            srvAesKey = Hex.decodeHex("4a4b85823ec83fe4fcbd08e579e3500788bd74777104934a52c0c71474d7dc44".toCharArray());
            cliIvKey = Hex.decodeHex("7589defd87a1d383f1d7f7193b50101e91e194a40b1b10c086e7d79f509f1f6e".toCharArray());
            srvIvKey = Hex.decodeHex("1af48f46fb08ca5cc0894b92b9a0cb3d934202666a24f06d230935672899e5ae".toCharArray());
        } catch (Exception e) {
            Assertions.assertThat(false);
        }
        BarclaysSession s = new BarclaysSession(seed, BarclaysConstants.RSA_PUB_KEY);
        Assertions.assertThat(s.getCliAesKey()).isEqualTo(cliAesKey);
        Assertions.assertThat(s.getSrvAesKey()).isEqualTo(srvAesKey);
        Assertions.assertThat(s.getCliIvKey()).isEqualTo(cliIvKey);
        Assertions.assertThat(s.getSrvIvKey()).isEqualTo(srvIvKey);
    }
}
