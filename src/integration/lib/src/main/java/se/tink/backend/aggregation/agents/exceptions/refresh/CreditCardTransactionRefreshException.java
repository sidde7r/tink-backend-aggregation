package se.tink.backend.aggregation.agents.exceptions.refresh;

public class CreditCardTransactionRefreshException extends TransactionRefreshException {

    public CreditCardTransactionRefreshException(String message) {
        super(message);
    }

    public CreditCardTransactionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
