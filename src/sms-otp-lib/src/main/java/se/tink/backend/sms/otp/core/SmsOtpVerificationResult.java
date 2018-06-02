package se.tink.backend.sms.otp.core;

public enum SmsOtpVerificationResult {
    /**
     * The requested OTP was not found.
     */
    OTP_NOT_FOUND,

    /**
     * The OTP had a status that wasn't eligible for verification.
     */
    INVALID_OTP_STATUS,

    /**
     * The OTP had expired.
     */
    OTP_EXPIRED,

    /**
     * Someone had tried to verify the same OTP too many times.
     */
    TOO_MANY_VERIFICATION_ATTEMPTS,

    /**
     * An incorrect code was provided.
     */
    INCORRECT_CODE,

    /**
     * The correct code was provided.
     */
    CORRECT_CODE,
}
