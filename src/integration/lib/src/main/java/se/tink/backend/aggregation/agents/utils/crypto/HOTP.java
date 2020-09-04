package se.tink.backend.aggregation.agents.utils.crypto;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class HOTP {
    public static String generateOTP(
            byte[] secret, long movingFactor, int codeDigits, int truncationOffset) {
        byte[] movingFactorByteArray = putMovingFactorIntoByteArray(movingFactor);
        byte[] hash = Hash.hmacSha1(secret, movingFactorByteArray);

        int offset = hash[hash.length - 1] & 0xf;
        if ((0 <= truncationOffset) && (truncationOffset < (hash.length - 4))) {
            offset = truncationOffset;
        }
        int binary =
                ((hash[offset] & 0x7f) << 24)
                        | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8)
                        | (hash[offset + 3] & 0xff);

        int otp = (int) (binary % Math.pow(10, codeDigits));
        String result = Integer.toString(otp);
        return StringUtils.leftPad(result, codeDigits, "0");
    }

    private static byte[] putMovingFactorIntoByteArray(long movingFactor) {
        byte[] text = new byte[8];
        for (int i = text.length - 1; i >= 0; i--) {
            text[i] = (byte) (movingFactor & 0xff);
            movingFactor >>= 8;
        }
        return text;
    }
}
