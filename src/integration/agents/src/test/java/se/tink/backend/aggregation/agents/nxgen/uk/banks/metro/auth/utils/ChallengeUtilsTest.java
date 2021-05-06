package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;

public class ChallengeUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void shouldShiftEcPublicKey() {
        // given
        KeyPair challengeSignECKeyPair = EllipticCurve.generateKeyPair(256);

        // when
        String shiftedPublicKey =
                ChallengeUtils.shiftEcPublicKey((ECPublicKey) challengeSignECKeyPair.getPublic());

        // then
        assertThat(shiftedPublicKey.getBytes()).hasSize(88);
    }

    @Test
    public void shouldSignDataWithShift() {
        // given
        String challengeValue = "U8NAh5ZcfLIIhXAfP2rG1yCB";
        KeyPair challengeSignECKeyPair = EllipticCurve.generateKeyPair(256);

        // when
        String shiftedPublicKey =
                ChallengeUtils.signDataWithShift(
                        challengeSignECKeyPair.getPrivate(), challengeValue);

        // then
        assertThat(shiftedPublicKey).hasSize(88);
    }
}
