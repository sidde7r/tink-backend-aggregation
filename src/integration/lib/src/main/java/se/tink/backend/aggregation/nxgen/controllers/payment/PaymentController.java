package se.tink.backend.aggregation.nxgen.controllers.payment;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) {
        return paymentExecutor.create(paymentRequest);
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return paymentExecutor.fetch(paymentRequest);
    }

    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        return paymentExecutor.sign(paymentMultiStepRequest);
    }

    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return paymentExecutor.createBeneficiary(createBeneficiaryMultiStepRequest);
    }

    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return paymentExecutor.cancel(paymentRequest);
    }

    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest) {
        return paymentExecutor.fetchMultiple(paymentRequest);
    }
}
