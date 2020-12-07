package se.tink.backend.aggregation.agents.exceptions.payment;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class DuplicatePaymentException extends PaymentException {
    public static final String DEFAULT_MESSAGE = EndUserMessage.DUPLICATE_PAYMENT.getKey().get();

    public DuplicatePaymentException() {
        super(DEFAULT_MESSAGE, InternalStatus.DUPLICATE_PAYMENT);
    }
}
