package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.payment.rpc.Payment;

/** This interface gives the possibility to create PaymentController based on Payment */
public interface TypedPaymentControllerable {

    Optional<PaymentController> getPaymentController(Payment payment);
}
