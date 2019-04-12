package se.tink.backend.aggregation.nxgen.controllers.payment;

public interface PaymentExecutor {
    PaymentResponse createPayment(PaymentRequest payment);
    PaymentResponse fetchPaymentStatus(PaymentRequest payment);
    PaymentMultiStepResponse signPayment(PaymentMultiStepRequest payment);
    PaymentMultiStepResponse createBeneficiary();
    PaymentResponse cancelPayment(PaymentRequest payment);
    PaymentListResponse fetchPayments();
}
