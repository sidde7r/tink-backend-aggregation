package se.tink.backend.sms.otp.core;

public enum SmsOtpEventType {
    /**
     * The SMS OTP was sent.
     */
    SMS_SENT,
    /**
     * The SMS OTP was verified to many times.
     */
    TOO_MANY_VERIFICATION_ATTEMPTS,
}
