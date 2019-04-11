package se.tink.backend.aggregation.nxgen.controllers.payment;

public interface PaymentExecutor {
    PaymentResponse createPayment(PaymentRequest payment);
    PaymentResponse fetchPaymentStatus(PaymentRequest payment);
    PaymentMultiStepResponse signPayment(PaymentRequest payment);
    PaymentMultiStepResponse createBeneficiary();
    PaymentResponse cancelPayment(PaymentRequest payment);
    PaymentListResponse fetchPayments();
}
