package se.tink.backend.aggregation.nxgen.controllers.payment;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentResponse {
    private Payment payment;
    private Storage paymentStorage;

    public PaymentResponse(Payment payment) {
        this.payment = payment;
        this.paymentStorage = new Storage();
    }

    public PaymentResponse(Payment payment, Storage paymentStorage) {
        this.payment = payment;
        this.paymentStorage = Storage.copyOf(paymentStorage);
    }

    public Payment getPayment() {
        return payment;
    }

    public boolean isStatus(PaymentStatus status) {
        return payment.getStatus().equals(status);
    }

    public ImmutableMap getPaymentStorage() {
        return paymentStorage.getImmutableCopy();
    }
}
