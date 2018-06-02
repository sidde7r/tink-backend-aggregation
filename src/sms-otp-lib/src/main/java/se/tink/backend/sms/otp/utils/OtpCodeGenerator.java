package se.tink.backend.sms.otp.utils;

import com.google.common.base.Preconditions;
import se.tink.backend.sms.otp.core.OtpType;

public class OtpCodeGenerator {

    private final static int OTP_MIN_LENGTH = 4;
    private final static int OTP_MAX_LENGTH = 8;
    /**
     * Generate a numeric otp code of the desired length.
     *
     * @param length must be between 4 and 8.
     * @return The generated code left padding with zeroes.
     */
    public static String generateCode(int length) {
        return generateCode(OtpType.NUMERIC, length);
    }

    /**
     * Generate a otp code of the desired length.
     *
     * @param type layout of the code used
     * @param length must be between 4 and 8.
     * @return The generated code of exactly size length
     */
    public static String generateCode(OtpType type, int length) {
        Preconditions.checkState(length >= OTP_MIN_LENGTH && length <= OTP_MAX_LENGTH,
                String.format("OTP length must be between %d and %d characters.", OTP_MIN_LENGTH, OTP_MAX_LENGTH));

        StringBuilder code = new StringBuilder();

        for (int i = length; i > 0; i--) {
            Character randomCharacter = type.getRandom();
            code.append(randomCharacter);
        }

        return code.toString();
    }

}
