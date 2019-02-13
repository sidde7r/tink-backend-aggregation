package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.DES;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.TripleDES;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class IngCryptoUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateDeviceIdHexString() {
        byte[] randomDeviceId = IngCryptoUtils.getRandomBytes(16);
        String randomDeviceIdString = EncodingUtils.encodeHexAsString(randomDeviceId);
        return Strings.padStart(randomDeviceIdString, 40, '0');
    }

    public static byte[] generateEncryptedQueryData(
            String registrationCode, byte[] sessionKey, byte[] sessionKeyAuth) {
        byte[] formattedRegistrationCode = getFormattedRegistrationCode(registrationCode);
        byte[] timestamp = getUtcTimestamp();
        byte[] dataToEncrypt =
                Bytes.concat(sessionKey, sessionKeyAuth, formattedRegistrationCode, timestamp);

        RSAPublicKey publicKey = generateRSAPublicKey();
        return RSA.encryptEcbOaepSha1Mgf1(publicKey, dataToEncrypt);
    }

    public static byte[] decryptAppCredentials(
            String encryptedCredentialsResponse, byte[] sessionKey, byte[] sessionKeyAuth) {
        byte[] responseInBytes = EncodingUtils.decodeBase64String(encryptedCredentialsResponse);
        byte[] message = Arrays.copyOfRange(responseInBytes, 0, 48);
        byte[] macValue = Arrays.copyOfRange(responseInBytes, 48, responseInBytes.length);

        verifyMacValue(sessionKeyAuth, message, macValue);

        return AES.decryptCbcNoPadding(sessionKey, new byte[16], message);
    }

    private static void verifyMacValue(byte[] key, byte[] message, byte[] macValue) {
        byte[] calculcatedMac = Hash.hmacSha256(key, message);

        if (!Arrays.equals(macValue, calculcatedMac)) {
            throw new IllegalStateException("MAC verification failed");
        }
    }

    public static byte[] deriveOtpKey(byte[] pinCode, byte[] secret0, byte[] secret1) {
        byte[] ctr = new byte[] {0x00, 0x00, 0x00, 0x01};

        byte[] block0 = Hash.hmacSha256(pinCode, Bytes.concat(secret0, ctr));
        byte[] block1 = Hash.hmacSha256(pinCode, block0);
        block0 = xor(block1, block0);
        block1 = Hash.hmacSha256(pinCode, block1);

        // key0 is the first 16 bytes of the xor'ed hmacs.
        byte[] key0 = Arrays.copyOfRange(xor(block1, block0), 0, 16);
        // iv is 16 zeroes
        byte[] iv = new byte[16];

        return AES.decryptCbcNoPadding(key0, iv, secret1);
    }

    public static int calculateOtpForAuthentication(byte[] key, int counter) {
        byte[] otpData = calcOtpData(key, counter);
        return buildOtpWithCounterAndData(counter, otpData);
    }

    public static int calcOtpForSigningTransfer(
            byte[] key, int counter, String challenge1, String challenge2) {
        byte[] otpData = calcOtpData(key, counter);
        return calcOtp(counter, challenge1, challenge2, otpData);
    }

    static int calcOtp(int counter, String challenge1, String challenge2, byte[] ac) {
        byte[] otpData = mixTds(challenge1, challenge2, ac);
        return buildOtpWithCounterAndData(counter, otpData);
    }

    private static byte[] mixTds(String challenge1, String challenge2, byte[] ac) {
        byte[] tds = getTdsFormattedDataToSign(challenge1, challenge2);
        byte[] nullIv = new byte[8];
        return DES.encryptCbcNoPadding(ac, nullIv, tds);
    }

    private static int buildOtpWithCounterAndData(int counter, byte[] otpData) {
        int otp = (counter & 0xff) << 16;
        otp |= (otpData[otpData.length - 8] & 0xff) << 8;
        otp |= otpData[otpData.length - 7] & 0xff;
        return otp;
    }

    private static byte[] calcOtpData(byte[] key, int counter) {
        // This should not happen in practice
        Preconditions.checkArgument(counter < 0xffff, "Counter overflowed");

        byte[] nullIv = new byte[8];

        byte byte0 = (byte) ((counter >> 8) & 0xff);
        byte byte1 = (byte) (counter & 0xff);

        byte[] ctr0 = new byte[] {byte0, byte1, (byte) 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] key0 = TripleDES.encryptEcbNoPadding(key, ctr0);

        byte[] ctr1 = new byte[] {byte0, byte1, (byte) 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] key1 = TripleDES.encryptEcbNoPadding(key, ctr1);

        byte[] ctr2 =
                new byte[] {
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x80,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x10,
                    (byte) 0x00,
                    byte0,
                    byte1,
                    (byte) 0xa5,
                    (byte) 0x00,
                    (byte) 0x03,
                    (byte) 0x04,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x80
                };
        byte[] tmp0 = DES.encryptCbcNoPadding(key0, nullIv, ctr2);

        // tmp0 is the last 8 bytes of the encrypted data (which contain the counter)
        tmp0 = Arrays.copyOfRange(tmp0, tmp0.length - 8, tmp0.length);

        byte[] tmp1 = DES.decryptEcbNoPadding(key1, tmp0);

        return DES.encryptEcbNoPadding(key0, tmp1);
    }

    public static byte[] getRandomBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte getRandomByte() {
        byte[] bytes = new byte[1];
        RANDOM.nextBytes(bytes);
        return bytes[0];
    }

    private static byte[] getFormattedRegistrationCode(String registrationCode) {
        byte[] regCodeBinary = EncodingUtils.decodeHexString(registrationCode);
        byte[] formattedRegistrationCode = new byte[8];

        formattedRegistrationCode[0] = (byte) (registrationCode.length() | 0x10);
        System.arraycopy(regCodeBinary, 0, formattedRegistrationCode, 1, regCodeBinary.length);

        for (int i = regCodeBinary.length + 1; i < 8; i++) {
            formattedRegistrationCode[i] = IngCryptoUtils.getRandomByte();
        }

        return formattedRegistrationCode;
    }

    private static byte[] getUtcTimestamp() {
        long currentTime = System.currentTimeMillis();
        return Longs.toByteArray(currentTime);
    }

    private static RSAPublicKey generateRSAPublicKey() {
        byte[] modulusBytes = EncodingUtils.decodeHexString(IngConstants.Crypto.RSA_MODULUS_IN_HEX);
        byte[] exponentBytes =
                EncodingUtils.decodeHexString(IngConstants.Crypto.RSA_EXPONENT_IN_HEX);

        return RSA.getPublicKeyFromModulusAndExponent(modulusBytes, exponentBytes);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        Preconditions.checkArgument(a.length == b.length, "Mismatch of block lengths");

        byte[] output = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            output[i] = (byte) (a[i] ^ b[i]);
        }
        return output;
    }

    static byte[] getTdsFormattedDataToSign(String challenge1, String challenge2) {
        List<Integer> nibbles = new ArrayList<>();

        for (int i = 0; i < challenge1.length(); i++) {
            nibbles.add(Character.getNumericValue(challenge1.charAt(i)));
        }

        // f is always a separator between first and second challenge
        nibbles.add(0xf);

        for (int i = 0; i < challenge2.length(); i++) {
            nibbles.add(Character.getNumericValue(challenge2.charAt(i)));
        }

        // pad with f if nibble list length is odd
        if (nibbles.size() % 2 != 0) {
            nibbles.add(0xf);
        }

        // always pad with 80 and then as many zeros that is needed to fill the last 8 byte (16
        // nibbles) block
        nibbles.add(0x8);

        while (nibbles.size() % 16 != 0) {
            nibbles.add(0x0);
        }

        byte[] output = new byte[nibbles.size() / 2];

        // put the nibbles into their respective bytes in the output data
        for (int i = 0; i < nibbles.size(); i += 2) {
            output[i / 2] = (byte) (((nibbles.get(i) << 4) & 0xf0) | (nibbles.get(i + 1) & 0x0f));
        }

        return output;
    }
}
