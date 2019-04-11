package se.tink.backend.aggregation.nxgen.controllers.payment;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    public PaymentResponse fetchPaymentStatus(PaymentRequest payment) {
        return paymentExecutor.fetchPaymentStatus(payment);
    }

    public PaymentMultiStepResponse signPayment(PaymentRequest payment) {
        return paymentExecutor.signPayment(payment);
    }

    public PaymentResponse cancelPayments(PaymentRequest payment) {
        return paymentExecutor.cancelPayment(payment);
    }
}
