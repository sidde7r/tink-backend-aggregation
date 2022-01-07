package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentPendingException extends PaymentException {

    public static final String MESSAGE = "Bank left payment in pending state";
    public static final InternalStatus DEFAULT_STATUS =
            InternalStatus.BANK_LEFT_PAYMENT_IN_PENDING_STATE;

    public PaymentPendingException(String message) {
        super(message, DEFAULT_STATUS);
    }

    public PaymentPendingException() {
        this(MESSAGE);
    }
}
