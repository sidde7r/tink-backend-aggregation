package se.tink.backend.aggregation.agents.exceptions.refresh;

public class CheckingTransactionRefreshException extends TransactionRefreshException {

    public CheckingTransactionRefreshException(String message) {
        super(message);
    }

    public CheckingTransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
