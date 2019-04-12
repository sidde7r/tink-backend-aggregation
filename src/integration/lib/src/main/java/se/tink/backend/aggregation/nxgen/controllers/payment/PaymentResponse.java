package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.libraries.payment.rpc.Payment;

public class PaymentResponse {
    protected Payment payment;

    public PaymentResponse(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
