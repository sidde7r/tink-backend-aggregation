package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public class FetchablePaymentController extends PaymentController {
    private FetchablePaymentExecutor fetchablePaymentExecutor;

    public FetchablePaymentController(FetchablePaymentExecutor fetchablePaymentExecutor) {
        super(fetchablePaymentExecutor);
        this.fetchablePaymentExecutor = fetchablePaymentExecutor;
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return fetchablePaymentExecutor.fetch(paymentRequest);
    }

    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return fetchablePaymentExecutor.fetchMultiple(paymentListRequest);
    }
}
