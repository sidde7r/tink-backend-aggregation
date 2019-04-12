package se.tink.backend.aggregation.nxgen.controllers.payment;

public interface PaymentExecutor {
    PaymentResponse createPayment(PaymentRequest paymentRequest);
    PaymentResponse fetchPaymentStatus(PaymentRequest paymentRequset);
    PaymentMultiStepResponse signPayment(PaymentMultiStepRequest paymentRequest);
    PaymentMultiStepResponse createBeneficiary();
    PaymentResponse cancelPayment(PaymentRequest paymentRequest);
    PaymentListResponse fetchPayments();
}
