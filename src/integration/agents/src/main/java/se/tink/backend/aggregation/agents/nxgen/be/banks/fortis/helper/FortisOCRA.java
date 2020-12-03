package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

/**
 * This is OCRA implementation taken from RFC https://tools.ietf.org/html/rfc6287
 *
 * <p>Adapted to current code quality rules
 */
public class FortisOCRA {

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * This method generates an OCRA HOTP value for the given set of parameters.
     *
     * @param ocraSuite the OCRA Suite
     * @param key the shared secret, HEX encoded
     * @param counter the counter that changes on a per use basis, HEX encoded
     * @param question the challenge question, HEX encoded
     * @param password a password that can be used, HEX encoded
     * @param sessionInformation Static information that identifies the current session, Hex encoded
     * @param timeStamp a value that reflects a time
     * @return A numeric String in base 10 that includes digits
     */
    public static String generateOCRA(
            String ocraSuite,
            String key,
            String counter,
            String question,
            String password,
            String sessionInformation,
            String timeStamp) {
        int codeDigits = 0;
        int ocraSuiteLength = ocraSuite.length();
        int counterLength = 0;
        int questionLength = 0;
        int passwordLength = 0;

        int sessionInformationLength = 0;
        int timeStampLength = 0;

        // The OCRASuites components
        String cryptoFunction = ocraSuite.split(":")[1];
        String dataInput = ocraSuite.split(":")[2];

        // How many digits should we return
        codeDigits = Integer.decode(cryptoFunction.substring(cryptoFunction.lastIndexOf("-") + 1));

        // The size of the byte array message to be encrypted
        // Counter
        if (dataInput.toLowerCase().startsWith("c")) {
            // Fix the length of the HEX string
            counter = StringUtils.leftPad(counter, 16, '0');
            counterLength = 8;
        }

        // Question - always 128 bytes
        if (dataInput.toLowerCase().startsWith("q") || (dataInput.toLowerCase().contains("-q"))) {
            question = StringUtils.rightPad(question, 256, '0');
            questionLength = 128;
        }

        passwordLength = getPasswordLength(dataInput);
        if (passwordLength > 0) {
            password = StringUtils.leftPad(password, passwordLength * 2, '0');
        }

        sessionInformationLength = getSessionInformationLength(dataInput);
        if (sessionInformationLength > 0) {
            sessionInformation =
                    StringUtils.leftPad(sessionInformation, sessionInformationLength * 2, '0');
        }

        // TimeStamp
        if (dataInput.toLowerCase().startsWith("t") || (dataInput.toLowerCase().contains("-t"))) {
            timeStamp = StringUtils.leftPad(timeStamp, 16, '0');
            timeStampLength = 8;
        }

        // Remember to add "1" for the "00" byte delimiter
        byte[] msg =
                new byte
                        [ocraSuiteLength
                                + counterLength
                                + questionLength
                                + passwordLength
                                + sessionInformationLength
                                + timeStampLength
                                + 1];

        // Put the bytes of "ocraSuite" parameters into the message
        byte[] bArray = ocraSuite.getBytes();
        System.arraycopy(bArray, 0, msg, 0, bArray.length);

        // Delimiter
        msg[bArray.length] = 0x00;

        // Put the bytes of "Counter" to the message
        // Input is HEX encoded
        if (counterLength > 0) {
            bArray = EncodingUtils.decodeHexString(counter);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1, bArray.length);
        }

        // Put the bytes of "question" to the message
        // Input is text encoded
        if (questionLength > 0) {
            bArray = EncodingUtils.decodeHexString(question);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1 + counterLength, bArray.length);
        }

        // Put the bytes of "password" to the message
        // Input is HEX encoded
        if (passwordLength > 0) {
            bArray = EncodingUtils.decodeHexString(password);
            System.arraycopy(
                    bArray,
                    0,
                    msg,
                    ocraSuiteLength + 1 + counterLength + questionLength,
                    bArray.length);
        }

        // Put the bytes of "sessionInformation" to the message
        // Input is text encoded
        if (sessionInformationLength > 0) {
            bArray = EncodingUtils.decodeHexString(sessionInformation);
            System.arraycopy(
                    bArray,
                    0,
                    msg,
                    ocraSuiteLength + 1 + counterLength + questionLength + passwordLength,
                    bArray.length);
        }

        // Put the bytes of "time" to the message
        // Input is text value of minutes
        if (timeStampLength > 0) {
            bArray = EncodingUtils.decodeHexString(timeStamp);
            System.arraycopy(
                    bArray,
                    0,
                    msg,
                    ocraSuiteLength
                            + 1
                            + counterLength
                            + questionLength
                            + passwordLength
                            + sessionInformationLength,
                    bArray.length);
        }

        bArray = EncodingUtils.decodeHexString(key);

        byte[] hash = hmac(cryptoFunction, bArray, msg);
        return transformToOtp(hash, codeDigits);
    }

    private static int getPasswordLength(String dataInput) {
        if (dataInput.toLowerCase().contains("psha1")) {
            return 20;
        }

        if (dataInput.toLowerCase().contains("psha256")) {
            return 32;
        }

        if (dataInput.toLowerCase().contains("psha512")) {
            return 64;
        }
        return 0;
    }

    private static int getSessionInformationLength(String dataInput) {
        if (dataInput.toLowerCase().contains("s064")) {
            return 64;
        }
        if (dataInput.toLowerCase().contains("s128")) {
            return 128;
        }
        if (dataInput.toLowerCase().contains("s256")) {
            return 256;
        }
        if (dataInput.toLowerCase().contains("s512")) {
            return 512;
        }
        return 0;
    }

    private static String transformToOtp(byte[] hash, int codeDigits) {
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        return String.format(String.format("%%0%dd", codeDigits), otp);
    }

    private static byte[] hmac(String cryptoFunction, byte[] keyBytes, byte[] text) {

        if (cryptoFunction.toLowerCase().contains("sha1")) return Hash.hmacSha1(keyBytes, text);
        if (cryptoFunction.toLowerCase().contains("sha256")) return Hash.hmacSha256(keyBytes, text);
        if (cryptoFunction.toLowerCase().contains("sha512")) return Hash.hmacSha512(keyBytes, text);

        throw new UnsupportedOperationException("Unsupported hashing function");
    }
}
