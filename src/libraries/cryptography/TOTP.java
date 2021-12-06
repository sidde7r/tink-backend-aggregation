package se.tink.libraries.cryptography;

import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;

public class TOTP {

    private static final int[] DIGITS_POWER
            // 0  1   2    3     4      5       6        7         8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    public static String generateTotpIgnoreLast4bitsHmacSha1(
            String hexKey, long epochS, int returnDigits) {
        StringBuilder result = null;
        byte[] hash;
        epochS <<= 4;
        byte[] keyBytes = EncodingUtils.decodeHexString(hexKey);
        StringBuilder timeKey = new StringBuilder(Long.toHexString(epochS));
        timeKey = new StringBuilder(timeKey.substring(0, timeKey.length() - 2));
        while (timeKey.length() < 16) timeKey.insert(0, "0");
        byte[] msg = EncodingUtils.decodeHexString(timeKey.toString());
        hash = Hash.hmacSha1(keyBytes, msg);
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[returnDigits];
        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < returnDigits) {
            result.insert(0, "0");
        }
        return result.toString();
    }
}
