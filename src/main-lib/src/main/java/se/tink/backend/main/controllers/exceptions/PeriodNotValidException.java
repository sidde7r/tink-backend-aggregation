package se.tink.backend.main.controllers.exceptions;

public class PeriodNotValidException extends IllegalArgumentException {
    public PeriodNotValidException (String message) {
        super(message);
    }
}
