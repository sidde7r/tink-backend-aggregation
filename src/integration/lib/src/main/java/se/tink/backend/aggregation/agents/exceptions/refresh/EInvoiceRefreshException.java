package se.tink.backend.aggregation.agents.exceptions.refresh;

public class EInvoiceRefreshException extends RefreshException {

    public EInvoiceRefreshException(String message) {
        super(message);
    }

    public EInvoiceRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
