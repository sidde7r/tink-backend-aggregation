package se.tink.backend.aggregation.agents.exceptions.payment;

public class InsufficientFundsException extends DebtorValidationException {

    public InsufficientFundsException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }

}
