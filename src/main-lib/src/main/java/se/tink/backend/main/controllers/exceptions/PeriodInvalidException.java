package se.tink.backend.main.controllers.exceptions;

public class PeriodInvalidException extends IllegalArgumentException {
    public PeriodInvalidException (String message) {
        super(message);
    }
}
