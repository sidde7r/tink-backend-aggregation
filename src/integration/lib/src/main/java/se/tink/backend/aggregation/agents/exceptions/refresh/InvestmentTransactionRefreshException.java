package se.tink.backend.aggregation.agents.exceptions.refresh;

public class InvestmentTransactionRefreshException extends TransactionRefreshException {

    public InvestmentTransactionRefreshException(String message) {
        super(message);
    }

    public InvestmentTransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
