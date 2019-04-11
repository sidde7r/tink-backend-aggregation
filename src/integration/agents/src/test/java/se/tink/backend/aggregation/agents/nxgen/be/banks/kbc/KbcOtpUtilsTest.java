package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.utils.KbcOtpUtils;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class KbcOtpUtilsTest {
    private static final String ACTIVATION_MESSAGE =
            "00415FDE868B554C5EE80E16E636C37BD5AABE0A45FFC25EA1F8AE9D58293D0195C924B38817F36667E0DC302E74E9D60AC61079E4594D03910B408E82E7";
    private static final String FINGERPRINT = "AGNr8IaCzyb7ZDBwGdpY0FUqV1PF2neVVWB3v0a4ZEsn0uHRrH";
    private static final String DECRYPTED_STATIC_VECTOR =
            "38080193010346444D0210CCC82A9FFACC98897B7988CA382C34680301010401040501090601010701000801030901040A01010B01000C01010E01010F01011001013801033C01014601064701011135120100130101140101150C4348414C4C524553502020201601011704018374D01801011901102101102901102A01002B01002C0102115F120100130101140102150C5349474E494E472020202020160101170401DD74D01801081901101A01001B01001C01001D01001E01001F01002001002101102201102301102401102501102601102701102801102901102A01002B01002C01021147120100130101140103150C5345434348414E20202020201601011704018314D01801041901101A01101B01101C01102101102201102301102401102901102A01002B01002C0102112F120100130101140104150C524553504F4E53452020202016010117044080F0021801002901062A01002B01002C01023D35120100130101140101150C454E524F4C4C4D454E5420201601011704018314D01801011901102101102901052A01002B01002C0102";
    private static final String EXPECTED_STATICVECTOR_FIELD2 = "ccc82a9ffacc98897b7988ca382c3468";
    private static final String DECRYPTED_DYNAMIC_VECTOR =
            "00005FDE868B55715755911267572800AF9D4AB4F5C48B719338F548BB5F4AFA30995577656196C23D74DEF43128A337A80AF89813A2";
    private static final String EXPECTED_OTP_KEY = "6524a717a516735fa14058250eb85ccd";

    @Test
    public void testLogonId() {
        String expectedLogonId = "8817493";
        byte[] dynamicVector = EncodingUtils.decodeHexString(DECRYPTED_DYNAMIC_VECTOR);
        String logonId = KbcOtpUtils.calculateLogonId(dynamicVector);
        Assertions.assertThat(expectedLogonId).isEqualTo(logonId);
    }

    @Test
    public void testDiversifier() {
        String expectedDiversifier = "000d6183";

        KbcDevice device = new KbcDevice();
        device.setFingerprint(FINGERPRINT).setStaticVector(DECRYPTED_STATIC_VECTOR);

        byte[] diversifier = device.calculateDiversifier();
        Assertions.assertThat(expectedDiversifier)
                .isEqualTo(EncodingUtils.encodeHexAsString(diversifier));
    }

    @Test
    public void testAes8() {
        String expectedOutput = "a8538366a5909067";
        byte[] key = EncodingUtils.decodeHexString("9b9e4eb9080bf828663e6bea70bb4ad7");
        byte[] data = EncodingUtils.decodeHexString("00000017000d1823");

        byte[] output = KbcOtpUtils.aes8(key, data);
        Assertions.assertThat(expectedOutput).isEqualTo(EncodingUtils.encodeHexAsString(output));
    }

    @Test
    public void testWbAes() {
        String expectedKey = "b02b25574e413883d4ec395eff24d49f";
        byte[] staticVectorField2 = EncodingUtils.decodeHexString(EXPECTED_STATICVECTOR_FIELD2);

        byte[] calculatedKey = KbcOtpUtils.wbAesEncrypt(staticVectorField2);
        Assertions.assertThat(expectedKey)
                .isEqualTo(EncodingUtils.encodeHexAsString(calculatedKey));
    }

    @Test
    public void testStaticVectorParse() {
        Optional<byte[]> staticVectorField2 =
                KbcOtpUtils.extractTlvField(
                        EncodingUtils.decodeHexString(DECRYPTED_STATIC_VECTOR), 4, 2, 0);

        Assertions.assertThat(EXPECTED_STATICVECTOR_FIELD2)
                .isEqualTo(EncodingUtils.encodeHexAsString(staticVectorField2.get()));
    }

    @Test
    public void testAesCtr() {
        final String expectedEncryptedData =
                "00000877831a60c21bc5b0340c288633e9da52fce1c300680db30ec4409f5098c7d73c0ca8c766";

        byte[] key = EncodingUtils.decodeHexString("0367218e1d4460baadcb3eafb2bab6b3");
        byte[] ctr = EncodingUtils.decodeHexString("beba176ee41583aa0000000000000000");
        byte[] data =
                EncodingUtils.decodeHexString(
                        "22dcd9f366b24ecb5f290514a66b320aea10c64cc9932690753db74c610ba0704ac2f50c651bc7");

        byte[] encryptedData = AES.encryptCtr(key, ctr, data);

        Assertions.assertThat(expectedEncryptedData)
                .isEqualTo(EncodingUtils.encodeHexAsString(encryptedData));
    }

    @Test
    public void testCalculateOtpKey() {
        KbcDevice device = new KbcDevice();
        device.setStaticVector(DECRYPTED_STATIC_VECTOR)
                .setDynamicVector(DECRYPTED_DYNAMIC_VECTOR)
                .setActivationMessage(ACTIVATION_MESSAGE)
                .calculateOtpKey();

        Assertions.assertThat(EXPECTED_OTP_KEY)
                .isEqualTo(EncodingUtils.encodeHexAsString(device.getOtpKey()));
    }

    @Test
    public void testCalculateOtp() {
        String expectedOtp = "5136380928012489";
        String challenge = "0191199494958090";

        KbcDevice device = new KbcDevice();
        device.setStaticVector(DECRYPTED_STATIC_VECTOR)
                .setDynamicVector(DECRYPTED_DYNAMIC_VECTOR)
                .setActivationMessage(ACTIVATION_MESSAGE)
                .setFingerprint(FINGERPRINT)
                .calculateOtpKey();

        String otp = device.calculateAuthenticationOtp(challenge);
        Assertions.assertThat(expectedOtp).isEqualTo(otp);
    }

    @Test
    public void testCalculateDeviceCode() {
        String expectedDeviceCode = "22162545579";
        String challenge = "7157559112675728";

        KbcDevice device = new KbcDevice();
        device.setStaticVector(DECRYPTED_STATIC_VECTOR)
                .setDynamicVector(DECRYPTED_DYNAMIC_VECTOR)
                .setActivationMessage(ACTIVATION_MESSAGE)
                .setFingerprint(FINGERPRINT)
                .calculateOtpKey();

        String deviceCode = device.calculateDeviceCode(challenge);
        Assertions.assertThat(expectedDeviceCode).isEqualTo(deviceCode);
    }

    @Test
    public void testCalculateVerificationMessage() {
        String expectedVerificationMessage = "6302322139632588";

        KbcDevice device = new KbcDevice();
        device.setStaticVector(DECRYPTED_STATIC_VECTOR)
                .setDynamicVector(DECRYPTED_DYNAMIC_VECTOR)
                .setActivationMessage(ACTIVATION_MESSAGE)
                .setFingerprint(FINGERPRINT)
                .calculateOtpKey();

        String verificationMessage = device.calculateVerificationMessage();
        Assertions.assertThat(expectedVerificationMessage).isEqualTo(verificationMessage);
    }

    @Test
    public void testSerialization() {
        KbcDevice device = new KbcDevice();
        device.setStaticVector(DECRYPTED_STATIC_VECTOR)
                .setDynamicVector(DECRYPTED_DYNAMIC_VECTOR)
                .setActivationMessage(ACTIVATION_MESSAGE)
                .setFingerprint(FINGERPRINT)
                .calculateOtpKey();
        Assertions.assertThat(EXPECTED_OTP_KEY)
                .isEqualTo(EncodingUtils.encodeHexAsString(device.getOtpKey()));

        String serializedDevice = SerializationUtils.serializeToString(device);

        KbcDevice device2 =
                SerializationUtils.deserializeFromString(serializedDevice, KbcDevice.class);
        Assertions.assertThat(EXPECTED_OTP_KEY)
                .isEqualTo(EncodingUtils.encodeHexAsString(device2.getOtpKey()));
    }
}
