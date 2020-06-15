package se.tink.backend.aggregation.agents.exceptions.refresh;

public class TransactionRefreshException extends RefreshException {

    public TransactionRefreshException(String message) {
        super(message);
    }

    public TransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
