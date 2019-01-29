package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class XfadUtils {
    // The layout of xfad is the following:
    // [length] [staticVector] [otp_seed_iv] [otp_seed_data] [checksum]
    //
    // Note that [otp_seed_iv] and [otp_seed_data] are not byte aligned. That is why the `remainder` is
    // handled as an hex encoded string (operate on nibbles instead of bytes)

    private static final int OTP_SEED_IV_LENGTH = 7;
    private static final int OTP_SEED_DATA_LENGTH = 16;

    private static int getStaticVectorLengthFromXfad(byte[] xfad) {
        byte lengthIndicator = (byte)(xfad[1] & 0xf);
        int length = 56;
        if ((lengthIndicator & 0xf8) > 0) {
            length = xfad[3] | (xfad[2] << 8) | 4;
        }

        Preconditions.checkArgument(length < xfad.length, "Xfad length is invalid.");
        Preconditions.checkArgument(length > 4, "Xfad length is invalid.");

        return length;
    }

    public static byte[] getStaticVector(byte[] xfad) {
        int staticVectorLength = getStaticVectorLengthFromXfad(xfad);
        return Arrays.copyOfRange(xfad, 4, staticVectorLength);
    }

    public static String getRemainderHexEncoded(byte[] xfad) {
        int staticVectorLength = getStaticVectorLengthFromXfad(xfad);
        byte[] remainder = Arrays.copyOfRange(xfad, staticVectorLength, xfad.length);
        return EncodingUtils.encodeHexAsString(remainder);
    }

    private static byte[] extractOtpSeedIvArray(String xfadRemainder) {
        byte[] remainderAsChars = xfadRemainder.getBytes();
        Preconditions.checkArgument(
                remainderAsChars.length >= OTP_SEED_IV_LENGTH,
                "Xfad is too short for OTP seed IV."
        );
        return Arrays.copyOf(remainderAsChars, OTP_SEED_IV_LENGTH);
    }

    public static byte[] getOtpSeedIv(byte[] xfad) {
        String remainder = getRemainderHexEncoded(xfad);
        byte[] otpSeedIvArray = extractOtpSeedIvArray(remainder);
        return new byte[] {
                (byte)(otpSeedIvArray[0] + otpSeedIvArray[1]),
                (byte)(otpSeedIvArray[2] + otpSeedIvArray[3]),
                (byte)(otpSeedIvArray[4] + otpSeedIvArray[5]),
                otpSeedIvArray[5],
                otpSeedIvArray[6]
        };
    }

    public static byte[] getOtpSeedData(byte[] xfad) {
        String remainderAsString = getRemainderHexEncoded(xfad);
        String dataHex = remainderAsString.substring(OTP_SEED_IV_LENGTH, OTP_SEED_IV_LENGTH + OTP_SEED_DATA_LENGTH);

        Preconditions.checkArgument(
                dataHex.length() == OTP_SEED_DATA_LENGTH,
                "Xfad is too short for OTP seed data."
        );

        return EncodingUtils.decodeHexString(dataHex);
    }
}
