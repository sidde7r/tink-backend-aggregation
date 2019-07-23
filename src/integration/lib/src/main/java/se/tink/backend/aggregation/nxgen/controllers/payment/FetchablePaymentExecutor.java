package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public interface FetchablePaymentExecutor {

    PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException;

    PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException;
}
