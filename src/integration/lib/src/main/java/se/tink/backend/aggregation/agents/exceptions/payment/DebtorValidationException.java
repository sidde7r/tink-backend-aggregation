package se.tink.backend.aggregation.agents.exceptions.payment;

public class DebtorValidationException extends PaymentValidationException {
    public DebtorValidationException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }
}
