package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.serializer.KeyPairDeserializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.serializer.KeyPairSerializer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.utils.KbcOtpUtils;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KbcDevice {
    private static class StaticVectorKeys {
        static final int SEED = 2;
        static final int SIGNATURE = 1;
        static final int INITIAL_VALUE = 60;
        static final int DIVERSIFIER_LENGTH = 70;
        static final int SUBSECTION = 17;
        static final int SUBSECTION_TYPE = 20;
        static final int SUBSECTION_NAME = 21;
    }

    private static final int SIGNATURE_SUBSECTION_TYPE = 2;
    private static final int SECCHAN_SUBSECTION_TYPE = 3;

    private static final String CURVE_NAME = "secp256r1";

    // generated by us
    @JsonSerialize(using = KeyPairSerializer.class)
    @JsonDeserialize(using = KeyPairDeserializer.class)
    private KeyPair ellipticCurveKeyPair;
    private String fingerprint;

    // get from server
    private String deviceId;
    private String accessNumber;
    private byte[] staticVector;
    private byte[] dynamicVector;
    private byte[] activationMessage;
    private int deviceCounter;

    // starts at 1
    private int otpCounter = 1;
    private int signatureOtpCounter = 1;

    // generated from dynamicVector
    private byte[] decryptedActivationMessage;

    // keys
    private byte[] otpKey;
    private byte[] key0;
    private byte[] key1;
    private byte[] key2;
    private byte[] key3;
    private byte[] key4;

    public KbcDevice() {
        generateKeyPair();
        generateFingerprint();
    }

    private void generateKeyPair() {
        ellipticCurveKeyPair = EllipticCurve.generateKeyPair(CURVE_NAME);
    }

    private void generateFingerprint() {
        fingerprint = RandomStringUtils.randomAlphanumeric(KbcConstants.Encryption.FINGERPRINT_LENGTH);
    }

    public static byte[] generateIv() {
        return RandomUtils.secureRandom(KbcConstants.Encryption.IV_LENGTH);
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public KbcDevice setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public KbcDevice setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public KbcDevice setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
        return this;
    }

    @JsonIgnore
    public KeyPair getKeyPair() {
        return ellipticCurveKeyPair;
    }

    public String getStaticVector() {
        return EncodingUtils.encodeHexAsString(staticVector).toUpperCase();
    }

    public KbcDevice setStaticVector(String staticVector) {
        this.staticVector = EncodingUtils.decodeHexString(staticVector);
        return this;
    }

    public String getDynamicVector() {
        return EncodingUtils.encodeHexAsString(dynamicVector).toUpperCase();
    }

    public KbcDevice setDynamicVector(String dynamicVector) {
        this.dynamicVector = EncodingUtils.decodeHexString(dynamicVector);
        return this;
    }

    public String getActivationMessage() {
        return EncodingUtils.encodeHexAsString(activationMessage).toUpperCase();
    }

    public KbcDevice setActivationMessage(String activationMessage) {
        this.activationMessage = EncodingUtils.decodeHexString(activationMessage);
        return this;
    }

    public int getDeviceCounter() {
        return deviceCounter;
    }

    @VisibleForTesting
    byte[] getOtpKey() {
        return otpKey;
    }

    @VisibleForTesting
    byte[] calculateOtpKey() {
        checkCalculateOtpKey();
        return otpKey;
    }

    private Optional<byte[]> extractFieldName(int subsectionType) {
        Preconditions.checkNotNull(staticVector, "Static vector has not been set.");

        for (int i=0; ; i++) {
            Optional<byte[]> subSection = KbcOtpUtils.extractTlvField(staticVector, 4,
                    StaticVectorKeys.SUBSECTION, i);
            if (!subSection.isPresent()) {
                return Optional.empty();
            }

            int subSectionType = KbcOtpUtils.extractTlvFieldAsInt(subSection.get(), 0,
                    StaticVectorKeys.SUBSECTION_TYPE);
            if (subSectionType != subsectionType) {
                continue;
            }

            return KbcOtpUtils.extractTlvField(subSection.get(), 0, StaticVectorKeys.SUBSECTION_NAME, 0);
        }
    }

    byte[] calculateDiversifier() {
        Preconditions.checkNotNull(staticVector, "Static vector has not been set.");

        byte[] fpHash = Hash.sha256(fingerprint);
        long v = (fpHash[3] & 0xff) |
                ((fpHash[2] & 0xff) << 8) |
                ((fpHash[1] & 0xff) << 16) |
                ((fpHash[0] & 0xff) << 24);

        int CONSTANT = 3; // Unknown from where this value is taken from, possibly type `8` or `56`
        v = (CONSTANT + 32 * (v & 0xffffffffL)) & 0xffffffffL;

        long i = KbcOtpUtils.extractTlvFieldAsLong(staticVector, 4, StaticVectorKeys.INITIAL_VALUE);
        int pow = KbcOtpUtils.extractTlvFieldAsInt(staticVector, 4, StaticVectorKeys.DIVERSIFIER_LENGTH);
        for (int j=0; j<pow; j++) {
            i *= 10;
        }

        int ret = (int)(v - (v/i * i));

        return KbcOtpUtils.intToBytes(ret);
    }

    private void internalCalculateKeys() {
        Preconditions.checkNotNull(staticVector, "Static vector has not been set.");
        Preconditions.checkNotNull(dynamicVector, "Dynamic vector has not been set.");

        byte[] seed = KbcOtpUtils.extractTlvField(staticVector, 4, StaticVectorKeys.SEED, 0)
                .orElseThrow(() -> new IllegalStateException("Could not find seed in static vector."));
        byte[] signature = KbcOtpUtils.extractTlvField(staticVector, 4, StaticVectorKeys.SIGNATURE, 0)
                .orElseThrow(() -> new IllegalStateException("Could not find signature in static vector."));

        String logonId = KbcOtpUtils.calculateLogonId(dynamicVector);

        // Encrypt the seed with an obfuscated (whitebox) key
        key0 = KbcOtpUtils.wbAesEncrypt(seed);
        key1 = KbcOtpUtils.encryptLogonId(key0, signature, logonId);
        key2 = KbcOtpUtils.decryptDynamicVector(key1, dynamicVector);
        key3 = KbcOtpUtils.encryptConstantA0(key2);
    }

    private void checkCalculateKeys() {
        if (key0 == null || key1 == null || key2 == null || key3 == null) {
            internalCalculateKeys();
        }
    }

    private void internalCalculateOtpKey() {
        checkCalculateKeys();
        Preconditions.checkNotNull(activationMessage, "Activation message has not been set.");

        decryptedActivationMessage = KbcOtpUtils.decryptActivationMessage(key3, activationMessage);
        deviceCounter = KbcOtpUtils.extractDeviceCount(decryptedActivationMessage);
        key4 = KbcOtpUtils.extractKey4(decryptedActivationMessage);

        otpKey = AES.decryptEcbNoPadding(key2, key4);
    }

    private void checkCalculateOtpKey() {
        if (otpKey == null) {
            internalCalculateOtpKey();
        }
    }

    private String internalCalculateOtp(byte[] key, int counter, List<byte[]> challenges) {
        byte[] diversifier = calculateDiversifier();

        byte[] mashedChallenge = KbcOtpUtils.mashupChallenge(key, diversifier, counter, challenges);
        byte[] encryptedMashedChallenge = KbcOtpUtils.aes8(key, mashedChallenge);
        byte[] convertedChallenge = KbcOtpUtils.convertOtpResponse(encryptedMashedChallenge);

        return KbcOtpUtils.convertOtpToAscii(convertedChallenge);
    }

    public String calculateDeviceCode(String challenge) {
        checkCalculateKeys();
        String otp = internalCalculateOtp(key2, 0,
                Collections.singletonList(EncodingUtils.decodeHexString(challenge)));

        byte[] bDiversifier = calculateDiversifier();
        String diversifier = Integer.toString(KbcOtpUtils.bytesToInt(bDiversifier));
        int diversifierLength =
                KbcOtpUtils.extractTlvFieldAsInt(staticVector, 4, StaticVectorKeys.DIVERSIFIER_LENGTH);
        diversifier = StringUtils.leftPad(diversifier, diversifierLength, '0');

        // last 5 digits of the otp
        otp = otp.substring(otp.length() - 5);

        String modifiedDiversifier = KbcOtpUtils.modifyDiversifier(diversifier, otp);
        return modifiedDiversifier + otp;
    }

    public String calculateVerificationMessage() {
        checkCalculateOtpKey();
        Preconditions.checkState(deviceCounter <= 99, "Device counter cannot be over 99.");

        byte[] secChanFieldName = extractFieldName(SECCHAN_SUBSECTION_TYPE)
                .orElseThrow(() -> new IllegalStateException("Could not find SECCHAN field in static vector."));

        // The device count must be 0-padded and always 2 bytes long (hence the 99 maximum value)
        String sDeviceCount = String.format("%02d", deviceCounter);

        // The total length of the secchan plaintext must be 12 (10 + 2)
        byte[] secChanData = Bytes.concat(Arrays.copyOfRange(secChanFieldName, 0, 10),
                sDeviceCount.getBytes());

        byte[] encryptedSecChan = AES.encryptEcbPkcs5(otpKey, secChanData);

        byte[] activationMessageHash = Hash.sha256(activationMessage);
        List<byte[]> challenges = extractChallenges(activationMessageHash);

        return internalCalculateOtp(encryptedSecChan, 0, challenges);
    }

    public String calculateSignatureOtp(List<String> dataFields) {
        return calculateSignatureOtp(signatureOtpCounter++, dataFields);
    }

    @VisibleForTesting
    String calculateSignatureOtp(int signatureOtpCounter, List<String> dataFields) {
        checkCalculateOtpKey();
        Preconditions.checkState(deviceCounter <= 99, "Device counter cannot be over 99.");

        byte[] signatureFieldName = extractFieldName(SIGNATURE_SUBSECTION_TYPE)
                .orElseThrow(() -> new IllegalStateException("Could not find SIGNATURE field in static vector."));

        // The device count must be 0-padded and always 2 bytes long (hence the 99 maximum value)
        String sDeviceCount = String.format("%02d", deviceCounter);

        // The total length of the signature plaintext must be 12 (10 + 2)
        byte[] signatureData = Bytes.concat(Arrays.copyOfRange(signatureFieldName, 0, 10),
                sDeviceCount.getBytes());

        byte[] encryptedSignature = AES.encryptEcbPkcs5(otpKey, signatureData);
        byte[] dataFieldsByteArray = createDataFieldsByteArray(dataFields);
        List<byte[]> challenges = extractChallenges(dataFieldsByteArray);

        return internalCalculateOtp(encryptedSignature, signatureOtpCounter, challenges);
    }

    private List<byte[]> extractChallenges(byte[] challengesByteArray) {
        List<byte[]> challenges = new ArrayList<>();
        // Extract 8 byte chunks from the byte array and treat them as challenges
        for (int i = 0; i < challengesByteArray.length; i += 8) {
            byte[] challenge = Arrays.copyOfRange(challengesByteArray, i, i + 8);
            challenges.add(challenge);
        }
        return challenges;
    }

    private byte[] createDataFieldsByteArray(List<String> dataFields) {
        int dataFieldsWithoutPaddingLength = dataFields.stream().mapToInt(String::length).sum() + (dataFields.size()-1);

        int paddingSize = dataFieldsWithoutPaddingLength % 8;
        if (paddingSize > 0) {
            paddingSize = 8 - paddingSize;
        }
        int dataFieldsByteArrayLength = dataFieldsWithoutPaddingLength + paddingSize;

        ByteBuffer buffer = ByteBuffer.allocate(dataFieldsByteArrayLength);

        for (int i = 0; i < (dataFields.size()-1); i++) {
            buffer.put(dataFields.get(i).toUpperCase().getBytes());
            buffer.put((byte) 0x00);
        }
        buffer.put(dataFields.get(dataFields.size()-1).getBytes());
        for (int i = 0; i < paddingSize; i++) {
            buffer.put((byte) 0xdd);
        }

        return buffer.array();
    }

    public String calculateAuthenticationOtp(String challenge) {
        checkCalculateOtpKey();
        return internalCalculateOtp(otpKey, otpCounter++,
                Collections.singletonList(EncodingUtils.decodeHexString(challenge)));
    }

    public byte[] calculateSharedSecret(byte[] serverPublicKeyBytes) {
        ECPrivateKey clientPrivateKey = (ECPrivateKey) ellipticCurveKeyPair.getPrivate();

        ECPublicKey serverPublicKey = EllipticCurve.convertPointToPublicKey(
                Bytes.concat(new byte[] {0x04}, serverPublicKeyBytes), CURVE_NAME);

        return calculateSharedSecret(clientPrivateKey, serverPublicKey);
    }

    private static byte[] calculateSharedSecret(ECPrivateKey privateKey, ECPublicKey publicKey) {
        byte[] derivedKey = EllipticCurve.diffieHellmanDeriveKeyConcatXY(privateKey, publicKey);
        derivedKey = swapBytes(derivedKey);
        derivedKey = Hash.sha256(derivedKey);
        derivedKey = Arrays.copyOfRange(derivedKey, 0, 16);
        return derivedKey;
    }

    private static byte[] swapBytes(byte[] input) {
        byte[] result = new byte[64];
        for (int i = 0; i < 32; i++) {
            result[i] = input[31 - i];
            result[i + 32] = input[63 - i];
        }
        return result;
    }

    public String encryptClientPublicKeyAndNonce(byte[] aesKey, byte[] iv) {
        byte[] nonce = RandomUtils.secureRandom(4);

        byte[] clientPublicKey = EllipticCurve.convertPublicKeyToPoint(ellipticCurveKeyPair, false);
        clientPublicKey = Arrays.copyOfRange(clientPublicKey, 1, clientPublicKey.length);

        byte[] encryptedClientPublicKey = AES.encryptCfbSegmentationSize8NoPadding(aesKey, iv, clientPublicKey);
        byte[] encryptedClientPublicKeyAndNonce = Bytes.concat(encryptedClientPublicKey, nonce);

        return EncodingUtils.encodeHexAsString(encryptedClientPublicKeyAndNonce).toUpperCase();
    }
}
