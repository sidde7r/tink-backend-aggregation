package se.tink.backend.aggregation.agents.exceptions.refresh;

public class SavingsTransactionRefreshException extends TransactionRefreshException {

    public SavingsTransactionRefreshException(String message) {
        super(message);
    }

    public SavingsTransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
