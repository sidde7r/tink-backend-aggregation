package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentResponse {
    private Payment payment;

    public PaymentResponse(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    public boolean isStatus(PaymentStatus status) {
        return payment.getStatus().equals(status);
    }
}
