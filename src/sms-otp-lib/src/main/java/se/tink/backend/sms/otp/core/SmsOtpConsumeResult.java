package se.tink.backend.sms.otp.core;

public enum SmsOtpConsumeResult {
    /**
     * The OTP was consumed.
     */
    CONSUMED,

    /**
     * The OTP had a status that wasn't eligible for consumption.
     */
    INVALID_OTP_STATUS,

    /**
     * The OTP had already been consumed.
     */
    ALREADY_CONSUMED,
}
