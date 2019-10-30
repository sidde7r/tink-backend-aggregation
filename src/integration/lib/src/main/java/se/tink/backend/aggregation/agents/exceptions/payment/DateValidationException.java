package se.tink.backend.aggregation.agents.exceptions.payment;

public class DateValidationException extends PaymentValidationException {

    public DateValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }
}
