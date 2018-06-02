package se.tink.backend.main.controllers.exceptions;

public class InvalidSmsOtpStatusException extends Exception {
    public InvalidSmsOtpStatusException(String message) {
        super(message);
    }
}
