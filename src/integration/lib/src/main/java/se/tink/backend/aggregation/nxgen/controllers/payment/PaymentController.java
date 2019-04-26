package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return paymentExecutor.create(paymentRequest);
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return paymentExecutor.fetch(paymentRequest);
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

    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
        return paymentExecutor.fetchMultiple(paymentRequest);
    }
}
