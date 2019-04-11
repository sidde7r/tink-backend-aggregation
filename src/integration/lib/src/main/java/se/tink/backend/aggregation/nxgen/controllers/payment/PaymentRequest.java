package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.libraries.payment.rpc.Payment;

public class PaymentRequest {
    private Payment payment;

    public PaymentRequest(Payment payment) {
        this.payment = payment;
    }


    public Payment getPayment() {
        return payment;
    }
}
