package se.tink.backend.aggregation.agents.utils.crypto;

import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class HOTP {
    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    public static String generateOTP(
            byte[] secret, long movingFactor, int codeDigits, int truncationOffset) {
        byte[] text = new byte[8];
        for (int i = text.length - 1; i >= 0; i--) {
            text[i] = (byte) (movingFactor & 0xff);
            movingFactor >>= 8;
        }
        byte[] hash = Hash.hmacSha1(secret, text);

        int offset = hash[hash.length - 1] & 0xf;
        if ((0 <= truncationOffset) && (truncationOffset < (hash.length - 4))) {
            offset = truncationOffset;
        }
        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];
        String result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }
}
