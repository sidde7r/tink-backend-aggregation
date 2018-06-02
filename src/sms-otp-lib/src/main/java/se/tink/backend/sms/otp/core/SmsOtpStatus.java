package se.tink.backend.sms.otp.core;

public enum SmsOtpStatus {
    /**
     * The OTP has not yet been sent.
     */
    NOT_SENT,
    /**
     * The OTP has been sent successfully.
     */
    SENT_SUCCESS,
    /**
     * The OTP could not be sent.
     */
    SENT_FAILED,
    /**
     * The OTP has been verified.
     */
    VERIFIED,
    /**
     * The OTP has been consumed.
     */
    CONSUMED,
}
