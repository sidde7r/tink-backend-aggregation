package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass;

import com.google.common.primitives.Bytes;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.ActivationMessage2;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.CryptoUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.DynamicVector;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.FingerPrintUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.OtpUtils;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DigipassV2 {
    // Configuration for how the OTP is calculated.
    // These can in the future be derived from the staticVector,
    // don't know how to do it as of yet.
    private final boolean useCounterBasedOtp;
    private final boolean useTimeBadesOtp;

    private int otpCounter = 1;

    // Generated by client
    private String fingerPrint;

    // Generated by server
    private byte[] dynamicVector;
    private byte[] staticVector;
    private byte[] activationMessage2;
    private byte[] digipassId;
    private int deviceCounter;

    // Phase 1 keys
    private byte[] key0;
    private byte[] key1;
    private byte[] key2;
    private byte[] key2_a0;
    private byte[] key2_a1;

    // Phase 2 keys
    private byte[] otpKey;
    private byte[] key3;
    private byte[] key3_b2;
    private byte[] key3_b3;

    public DigipassV2(boolean useCounterBasedOtp, boolean useTimeBadesOtp) {
        this.useCounterBasedOtp = useCounterBasedOtp;
        this.useTimeBadesOtp = useTimeBadesOtp;
        this.fingerPrint = FingerPrintUtils.generateFingerPrint();
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    private boolean hasCalculatedPhase1Keys() {
        return Objects.nonNull(this.key0)
                && Objects.nonNull(this.key1)
                && Objects.nonNull(key2_a0)
                && Objects.nonNull(key2_a1);
    }

    private boolean calculatePhase1Keys() {
        if (Objects.isNull(this.dynamicVector) || Objects.isNull(this.staticVector)) {
            // Cannot calculate the following keys without these values.
            return false;
        }

        if (this.hasCalculatedPhase1Keys()) {
            // Do not repeat this task.
            return true;
        }

        StaticVector staticVector = StaticVector.createFromVector(this.staticVector);
        if (shouldUseWhiteboxCrypto(staticVector)) {
            // We don't handle this yet, we need a sample to implement it correctly.
            throw new IllegalStateException("Cannot handle whitebox crypto yet.");
        }

        byte[] initialCryptoKey =
                EncodingUtils.decodeHexString(DigipassConstants.FixedKeys.INITIAL_CRYPTO_KEY);
        byte[] initialCryptoSeed =
                staticVector
                        .getField(DigipassConstants.StaticVectorFieldType.CRYPTO_KEY)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find initial crypto seed in static vector."));

        this.key0 = AES.encryptEcbNoPadding(initialCryptoKey, initialCryptoSeed);

        byte[] signature =
                staticVector
                        .getField(DigipassConstants.StaticVectorFieldType.SIGNATURE)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find signature in static vector."));
        String logonId = DynamicVector.calculateLogonId(this.dynamicVector);
        this.key1 = CryptoUtils.encryptSignatureAndLogonId(this.key0, signature, logonId);

        this.key2 = DynamicVector.decryptDynamicVectorKey(this.key1, this.dynamicVector);

        this.key2_a0 = CryptoUtils.createSubKey(this.key2, (byte) 0xa0);
        this.key2_a1 = CryptoUtils.createSubKey(this.key2, (byte) 0xa1);

        return this.hasCalculatedPhase1Keys();
    }

    private boolean hasCalculatedPhase2Keys() {
        return Objects.nonNull(this.otpKey)
                && Objects.nonNull(this.key3)
                && Objects.nonNull(this.key3_b2)
                && Objects.nonNull(this.key3_b3);
    }

    private boolean calculatePhase2Keys() {
        if (Objects.isNull(this.dynamicVector)
                || Objects.isNull(this.staticVector)
                || Objects.isNull(this.activationMessage2)) {
            // Cannot calculate the following keys without these values.
            return false;
        }

        if (hasCalculatedPhase2Keys()) {
            // Do not repeat this task.
            return true;
        }

        this.digipassId = ActivationMessage2.extractDigipassId(this.activationMessage2);
        byte[] decryptedActivationMessage2 =
                ActivationMessage2.decrypt(this.key2_a0, this.activationMessage2);
        this.deviceCounter = ActivationMessage2.extractDeviceCount(decryptedActivationMessage2);

        this.otpKey = ActivationMessage2.decryptKey1(this.key2, decryptedActivationMessage2);

        this.key3 = ActivationMessage2.decryptKey2(this.key2, decryptedActivationMessage2);
        this.key3_b2 = CryptoUtils.createSubKey(this.key3, (byte) 0xb2);
        this.key3_b3 = CryptoUtils.createSubKey(this.key3, (byte) 0xb3);

        return hasCalculatedPhase2Keys();
    }

    private boolean shouldUseWhiteboxCrypto(StaticVector staticVector) {
        // This value is either 0, 1 or not present. Only the value 1 (boolean) means that it should
        // use whitebox crypto.
        return staticVector
                        .getFieldAsInt(DigipassConstants.StaticVectorFieldType.USE_WHITEBOX_CRYPTO)
                        .orElse(0)
                == 1;
    }

    public void setDynamicAndStaticVectors(String dynamicVectorAsHex, String staticVectorAsHex) {
        this.dynamicVector = EncodingUtils.decodeHexString(dynamicVectorAsHex);
        this.staticVector = EncodingUtils.decodeHexString(staticVectorAsHex);

        if (!this.calculatePhase1Keys()) {
            throw new IllegalStateException("Failed to calculate phase 1 keys.");
        }
    }

    public void setActivationMessage2(String activationMessage2AsHex) {
        if (!this.hasCalculatedPhase1Keys()) {
            throw new IllegalStateException(
                    "Cannot process activationMessage2 before phase 1 keys have been calculated");
        }

        this.activationMessage2 = EncodingUtils.decodeHexString(activationMessage2AsHex);
        if (!this.calculatePhase2Keys()) {
            throw new IllegalStateException("Failed to calculate phase 2 keys.");
        }
    }

    public String calculateDeviceCode(String challengeAsHex) {
        if (!this.hasCalculatedPhase1Keys()) {
            throw new IllegalStateException(
                    "Cannot calculate deviceCode before phase 1 keys have been calculated");
        }

        // Only one challenge "block" is used in deviceCode calculation.
        List<byte[]> challenges = OtpUtils.constructChallengeArray(challengeAsHex);
        StaticVector staticVector = StaticVector.createFromVector(this.staticVector);

        String otp =
                OtpUtils.calculateOtp(
                        this.fingerPrint, staticVector, this.key2, 0, 0, false, challenges);

        // Use only the last 5 digits of the otp.
        otp = otp.substring(otp.length() - 5);
        return OtpUtils.calculateDerivationCode(fingerPrint, staticVector, otp);
    }

    public String calculateDeviceCode() {
        // No challenge, i.e. all 0s in the xor block.
        return this.calculateDeviceCode(DigipassConstants.FixedKeys.NULL_CHALLENGE);
    }

    private long getCurrentEpoch() {
        return Instant.now().getEpochSecond();
    }

    public String calculateOtp(String challengeAsHex) {
        if (!this.hasCalculatedPhase2Keys()) {
            throw new IllegalStateException(
                    "Cannot calculate OTP before phase 2 keys have been calculated.");
        }

        List<byte[]> challenges = OtpUtils.constructChallengeArray(challengeAsHex);
        StaticVector staticVector = StaticVector.createFromVector(this.staticVector);

        int otpCounter = this.useCounterBasedOtp ? this.otpCounter : 0;
        long epochTime = this.useTimeBadesOtp ? this.getCurrentEpoch() : 0;

        String otp =
                OtpUtils.calculateOtp(
                        this.fingerPrint,
                        staticVector,
                        this.otpKey,
                        otpCounter,
                        epochTime,
                        false,
                        challenges);

        if (this.useCounterBasedOtp) {
            this.otpCounter += 1;
        }
        return otp;
    }

    public String createSecureChannelInformationMessage(String messageToEncrypt) {
        byte[] ctrNonce = RandomUtils.secureRandom(8);
        return this.createSecureChannelInformationMessage(messageToEncrypt, ctrNonce);
    }

    String createSecureChannelInformationMessage(String messageToEncrypt, byte[] counterNonce) {
        if (!this.hasCalculatedPhase2Keys()) {
            throw new IllegalStateException(
                    "Cannot create a Secure channel information message before phase 2 keys have been calculated.");
        }

        byte[] encryptedMessage =
                AES.encryptCtr(this.key3_b2, counterNonce, messageToEncrypt.getBytes());

        byte[] secureChannelInformationMessageWithoutMac =
                Bytes.concat(
                        new byte[] {
                            (byte) 0x09, (byte) 0x01
                        }, // Have not been able to derive where these values comes from.
                        this.digipassId,
                        counterNonce,
                        encryptedMessage);

        byte[] mac = Hash.hmacSha256(this.key3_b3, secureChannelInformationMessageWithoutMac);

        // Append the first 8 bytes of the mac to the final message.
        byte[] secureChannelInformationMessage =
                Bytes.concat(
                        secureChannelInformationMessageWithoutMac, Arrays.copyOfRange(mac, 0, 8));

        return EncodingUtils.encodeHexAsString(secureChannelInformationMessage);
    }

    public String serialize() {
        Map<String, String> m = new HashMap<>();

        m.put(DigipassConstants.Serialization.OTP_COUNTER, Integer.toString(otpCounter));

        Optional.ofNullable(fingerPrint)
                .ifPresent(v -> m.put(DigipassConstants.Serialization.FINGERPRINT, v));

        Optional.ofNullable(dynamicVector)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.DYNAMIC_VECTOR,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(staticVector)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.STATIC_VECTOR,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(activationMessage2)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.ACTIVATION_MESSAGE2,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(digipassId)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.DIGIPASS_ID,
                                        EncodingUtils.encodeAsBase64String(v)));

        m.put(DigipassConstants.Serialization.DEVICE_COUNTER, Integer.toString(deviceCounter));

        Optional.ofNullable(key0)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_0,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key1)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_1,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key2)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_2,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key2_a0)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_2_A0,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key2_a1)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_2_A1,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(otpKey)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.OTP_KEY,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key3)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_3,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key3_b2)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_3_B2,
                                        EncodingUtils.encodeAsBase64String(v)));

        Optional.ofNullable(key3_b3)
                .ifPresent(
                        v ->
                                m.put(
                                        DigipassConstants.Serialization.KEY_3_B3,
                                        EncodingUtils.encodeAsBase64String(v)));

        return SerializationUtils.serializeToString(m);
    }

    private Optional<String> getIfExists(Map<String, String> m, String key) {
        if (!m.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(m.get(key));
    }

    public void deserialize(String mstring) {
        Map<String, String> m =
                SerializationUtils.deserializeFromString(
                        mstring, TypeReferences.MAP_OF_STRING_STRING);

        getIfExists(m, DigipassConstants.Serialization.OTP_COUNTER)
                .ifPresent(v -> otpCounter = Integer.valueOf(v));

        getIfExists(m, DigipassConstants.Serialization.FINGERPRINT).ifPresent(v -> fingerPrint = v);

        getIfExists(m, DigipassConstants.Serialization.DYNAMIC_VECTOR)
                .ifPresent(v -> dynamicVector = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.STATIC_VECTOR)
                .ifPresent(v -> staticVector = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.ACTIVATION_MESSAGE2)
                .ifPresent(v -> activationMessage2 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.DIGIPASS_ID)
                .ifPresent(v -> digipassId = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.DEVICE_COUNTER)
                .ifPresent(v -> deviceCounter = Integer.valueOf(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_0)
                .ifPresent(v -> key0 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_1)
                .ifPresent(v -> key1 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_2)
                .ifPresent(v -> key2 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_2_A0)
                .ifPresent(v -> key2_a0 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_2_A1)
                .ifPresent(v -> key2_a1 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.OTP_KEY)
                .ifPresent(v -> otpKey = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_3)
                .ifPresent(v -> key3 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_3_B2)
                .ifPresent(v -> key3_b2 = EncodingUtils.decodeBase64String(v));

        getIfExists(m, DigipassConstants.Serialization.KEY_3_B3)
                .ifPresent(v -> key3_b3 = EncodingUtils.decodeBase64String(v));
    }
}
