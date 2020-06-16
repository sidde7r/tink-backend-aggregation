package se.tink.backend.aggregation.agents.exceptions.refresh;

public class CreditCardAccountRefreshException extends AccountRefreshException {

    public CreditCardAccountRefreshException(String message) {
        super(message);
    }

    public CreditCardAccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
