package se.tink.backend.aggregation.agents.exceptions.refresh;

public class SavingsAccountRefreshException extends AccountRefreshException {

    public SavingsAccountRefreshException(String message) {
        super(message);
    }

    public SavingsAccountRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
