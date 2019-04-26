package se.tink.backend.aggregation.agents.exceptions.payment;

public class CreditorValidationException extends PaymentValidationException {
    public CreditorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }
}
