package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;
    private final FetchablePaymentExecutor fetchablePaymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = null;
    }

    public PaymentController(
            PaymentExecutor paymentExecutor, FetchablePaymentExecutor fetchablePaymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = fetchablePaymentExecutor;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return paymentExecutor.create(paymentRequest);
    }

    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        return paymentExecutor.sign(paymentMultiStepRequest);
    }

    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return paymentExecutor.createBeneficiary(createBeneficiaryMultiStepRequest);
    }

    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return paymentExecutor.cancel(paymentRequest);
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetch(paymentRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetchMultiple(paymentListRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public boolean canFetch() {
        return fetchablePaymentExecutor != null;
    }
}
