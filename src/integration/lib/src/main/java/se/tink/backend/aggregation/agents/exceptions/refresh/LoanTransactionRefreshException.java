package se.tink.backend.aggregation.agents.exceptions.refresh;

public class LoanTransactionRefreshException extends TransactionRefreshException {

    public LoanTransactionRefreshException(String message) {
        super(message);
    }

    public LoanTransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
