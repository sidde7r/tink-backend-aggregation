package se.tink.backend.aggregation.nxgen.controllers.payment.exception;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

/**
 * This class is meant to be used in PaymentController in the same package. Purpose of it is to
 * transform any known AgentExceptions into some other exception that is correctly handled in
 * Payment flow context - by one of the handlers here:
 * se.tink.backend.aggregation.workers.commands.exceptions.handlers
 */
public interface PaymentControllerAgentExceptionMapper {

    // This enum is primarily needed to keep the old behaviour. Ideally, mapping should be the same
    // for every context!
    public enum PaymentControllerAgentExceptionMapperContext {
        CREATE,
        SIGN,
        OTHER
    }

    Optional<PaymentException> tryToMapToPaymentException(
            AgentException agentException, PaymentControllerAgentExceptionMapperContext context);
}
