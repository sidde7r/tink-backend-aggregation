package se.tink.backend.aggregation.nxgen.controllers.payment;

public interface PaymentExecutor {
    PaymentResponse create(PaymentRequest paymentRequest);
    PaymentResponse fetch(PaymentRequest paymentRequest);
    PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest);
    PaymentMultiStepResponse createBeneficiary();
    PaymentResponse cancel(PaymentRequest paymentRequest);
    PaymentListResponse fetchMultiple(PaymentRequest paymentRequest);
}
