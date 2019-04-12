package se.tink.backend.aggregation.nxgen.controllers.payment;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    public PaymentResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        return paymentExecutor.fetchPaymentStatus(paymentRequest);
    }

    public PaymentMultiStepResponse signPayment(PaymentMultiStepRequest paymentRequest) {
        return paymentExecutor.signPayment(paymentRequest);
    }

    public PaymentResponse cancelPayments(PaymentRequest paymentRequest) {
        return paymentExecutor.cancelPayment(paymentRequest);
    }
}
