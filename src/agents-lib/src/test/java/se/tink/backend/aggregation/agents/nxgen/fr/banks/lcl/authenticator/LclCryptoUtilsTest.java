package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class LclCryptoUtilsTest {

    @Test
    public void testPasswordXor() {
        String pin = "283149";
        String expectedOfuscatedPin = "MTswMjc6";
        int key = 3;

        String obfuscatedPin = LclCryptoUtils.computeXorPin(pin, key);
        String b64EncodedObfuscatedPin = EncodingUtils.encodeAsBase64String(obfuscatedPin);
        Assert.assertEquals(expectedOfuscatedPin, b64EncodedObfuscatedPin);
    }
}
