package se.tink.backend.aggregation.agents.exceptions.payment;

public class ReferenceValidationException extends PaymentValidationException {

    public ReferenceValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

    public ReferenceValidationException(String message) {
        super(message);
    }
}
