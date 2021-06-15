package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentCancelledException extends PaymentException {

    public static final String MESSAGE = "The payment was cancelled by the user.";
    public static final InternalStatus DEFAULT_STATUS =
            InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED;

    public PaymentCancelledException(String message) {
        super(message, DEFAULT_STATUS);
    }

    public PaymentCancelledException() {
        this(MESSAGE);
    }
}
