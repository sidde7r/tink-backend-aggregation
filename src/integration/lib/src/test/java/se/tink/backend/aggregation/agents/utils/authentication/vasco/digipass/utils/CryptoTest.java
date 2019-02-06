package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class CryptoTest {
    private static final String ACTIVATION_PASSWORD = "1Orcx2";
    private static final String EXPECTED_ACTIVATION_KEY = "522a416ec27fe1a55da2789e0f54c233";

    private static final String STATIC_VECTOR_KEY = "5e4532dfd9570783834970c140c764e3";
    private static final String OTP_IV = "4644516670693239";
    private static final String OTP_DATA = "7c0778deeefc6891";
    private static final String EXPECTED_OTP_KEY = "4a34522f1f6b8fe526a4de6a60cac1d1";

    @Test
    public void testDeriveActivationKey() {
        byte[] activationKey = CryptoUtils.deriveActivationKey(ACTIVATION_PASSWORD);
        String activationKeyHex = EncodingUtils.encodeHexAsString(activationKey);

        Assert.assertEquals("Activation key mismatch.", EXPECTED_ACTIVATION_KEY, activationKeyHex);
    }

    @Test
    public void testCalculateOtpKey() {
        byte[] key = EncodingUtils.decodeHexString(STATIC_VECTOR_KEY);
        byte[] iv = EncodingUtils.decodeHexString(OTP_IV);
        byte[] data = EncodingUtils.decodeHexString(OTP_DATA);

        byte[] otpKey = CryptoUtils.calculateOtpKey(key, iv, data);
        String otpKeyHex = EncodingUtils.encodeHexAsString(otpKey);

        Assert.assertEquals("OTP key mismatch.", EXPECTED_OTP_KEY, otpKeyHex);
    }
}
