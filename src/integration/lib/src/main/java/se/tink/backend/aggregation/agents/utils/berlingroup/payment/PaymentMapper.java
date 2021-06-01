package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.libraries.payment.rpc.Payment;

// Transform Tink Payment model into Agent-specific payment request class
public interface PaymentMapper<T> {

    T getPaymentRequest(Payment payment);

    T getRecurringPaymentRequest(Payment payment);
}
