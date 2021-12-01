package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;

/**
 * This class is meant to be used in PaymentController in the same package. Purpose of it is to
 * transform any known AgentExceptions into some other exception that is correctly handled in
 * Payment flow context - by one of the handlers here:
 * se.tink.backend.aggregation.workers.commands.exceptions.handlers
 */
public interface PaymentControllerAgentExceptionMapper {
    public enum PaymentControllerAgentExceptionMapperContext {
        CREATE,
        SIGN,
        OTHER
    }

    default Optional<RuntimeException> mapToPaymentException(AgentException agentException) {
        return mapToPaymentException(
                agentException, PaymentControllerAgentExceptionMapperContext.OTHER);
    }

    Optional<RuntimeException> mapToPaymentException(
            AgentException agentException, PaymentControllerAgentExceptionMapperContext context);
}
