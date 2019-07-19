package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentResponse {
    private Payment payment;
    private Storage storage;

    public PaymentResponse(Payment payment) {
        this.payment = payment;
        this.storage = new Storage();
    }

    public PaymentResponse(Payment payment, Storage storage) {
        this.payment = payment;
        this.storage = Storage.copyOf(storage);
    }

    public static PaymentResponse of(PaymentRequest paymentRequest) {
        return new PaymentResponse(
                paymentRequest.getPayment(), Storage.copyOf(paymentRequest.getStorage()));
    }

    public Payment getPayment() {
        return payment;
    }

    public boolean isStatus(PaymentStatus status) {
        return payment.getStatus().equals(status);
    }

    public Storage getStorage() {
        return Storage.copyOf(storage);
    }
}
