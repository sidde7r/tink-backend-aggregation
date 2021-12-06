package se.tink.libraries.cryptography;

import org.junit.Assert;
import org.junit.Test;

public class TOTPTest {

    private static final String HEX_KEY = "ca3f836f2af80a1835a6a71575532a9e05291010";
    private static final long TIME = 0x05df38fd0;
    private static final String EXPECTED = "725709";

    @Test
    public void testTOTPGeneration() {

        String generatedOtp = TOTP.generateTotpIgnoreLast4bitsHmacSha1(HEX_KEY, TIME, 6);
        Assert.assertEquals(EXPECTED, generatedOtp);
    }
}
