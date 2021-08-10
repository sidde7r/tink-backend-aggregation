package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentException extends Exception {
    private static final String DEFAULT_MESSAGE = "Payment failed.";
    private final InternalStatus internalStatus;

    public PaymentException(String message) {
        super(message);
        this.internalStatus = null;
    }

    public PaymentException(InternalStatus internalStatus) {
        super(DEFAULT_MESSAGE);
        this.internalStatus = internalStatus;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.internalStatus = null;
    }

    public PaymentException(String message, InternalStatus internalStatus, Throwable cause) {
        super(message, cause);
        this.internalStatus = internalStatus;
    }

    public PaymentException(String message, InternalStatus internalStatus) {
        super(message);
        this.internalStatus = internalStatus;
    }

    public String getInternalStatus() {
        return internalStatus != null ? internalStatus.toString() : null;
    }
}
