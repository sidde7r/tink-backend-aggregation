package se.tink.backend.aggregation.agents.payments;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

/**
 * This interface exists as a temporary remedy for the poor design decision of doing type
 * introspection for SubsequentGenerationAgent.
 */
public interface PaymentControllerable {

    Optional<PaymentController> getPaymentController();
}
