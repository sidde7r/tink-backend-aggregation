package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

import java.util.Optional;

@Deprecated
public interface ConstructPaymentsRevampController {
    Optional<PaymentController> constructPaymentController();
}
