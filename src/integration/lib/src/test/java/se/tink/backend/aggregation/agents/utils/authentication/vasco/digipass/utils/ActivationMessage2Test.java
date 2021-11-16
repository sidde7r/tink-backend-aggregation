package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.encoding.EncodingUtils;

public class ActivationMessage2Test {
    private static final String ACTIVATION_MESSAGE2_AS_HEX =
            "00415FDF47AA6B7725E17B2ACA71CFB4E52935018A07C11E0213995BE9F6CC7A7A81C45B594223A0F4394DC8C1891F4CC1C06F351E6BA86251D526F3B233";

    @Before
    public void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testActivationMessage2Decrypt() {
        byte[] activationMessage2 = EncodingUtils.decodeHexString(ACTIVATION_MESSAGE2_AS_HEX);
        byte[] activationMessage2Key =
                EncodingUtils.decodeHexString("d7b654cbb0f69a87ffb8ecc6f3274073");

        byte[] decryptedMessage =
                ActivationMessage2.decrypt(activationMessage2Key, activationMessage2);

        Assert.assertEquals(
                "Decrypted ActivationMessage2 mismatch.",
                "000007d80305d419fb5425241cf25038068603d93f17001d27679ee450292e7bf4f8078cfcb7e0",
                EncodingUtils.encodeHexAsString(decryptedMessage));

        int deviceCounter = ActivationMessage2.extractDeviceCount(decryptedMessage);
        Assert.assertEquals("Device count in ActivationMessage2 mismatch.", 5, deviceCounter);

        byte[] keysKey = EncodingUtils.decodeHexString("e61bf5f0afc4e89536b0fb07bf3b788c");
        byte[] decryptedKey1 = ActivationMessage2.decryptKey1(keysKey, decryptedMessage);
        Assert.assertEquals(
                "Decrypted ActivationMessage2::key1 mismatch.",
                "2e3b1c2edc056bb6c7995555c2fec3ac",
                EncodingUtils.encodeHexAsString(decryptedKey1));

        byte[] decryptedKey2 = ActivationMessage2.decryptKey2(keysKey, decryptedMessage);
        Assert.assertEquals(
                "Decrypted ActivationMessage2::key2 mismatch.",
                "73e9a65ad3254884dbacaf55904e6340",
                EncodingUtils.encodeHexAsString(decryptedKey2));
    }
}
