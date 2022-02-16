package se.tink.backend.aggregation.nxgen.controllers.payment.validation;

import se.tink.libraries.payment.rpc.Payment;

public interface PaymentInitializationValidator {

    // This is supposed to block any payment initialization early on, before it even gets into any
    // Agent code.
    void throwIfNotPossibleToInitialize(Payment payment);
}
