package se.tink.backend.sms.otp.core.exceptions;

import java.util.UUID;

public class SmsOtpNotFoundException extends Exception {
    public SmsOtpNotFoundException(UUID id) {
        super(String.format("Sms otp could not be found: %s", id));
    }
}

