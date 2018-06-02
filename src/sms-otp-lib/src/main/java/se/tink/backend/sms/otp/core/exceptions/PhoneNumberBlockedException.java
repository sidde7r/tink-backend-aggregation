package se.tink.backend.sms.otp.core.exceptions;

public class PhoneNumberBlockedException extends Exception {
    public PhoneNumberBlockedException(String message) {
        super(message);
    }
}
