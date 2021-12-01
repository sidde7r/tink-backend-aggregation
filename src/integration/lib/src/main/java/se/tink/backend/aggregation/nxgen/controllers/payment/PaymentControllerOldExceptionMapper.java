package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentConstants.BankId;
import se.tink.libraries.signableoperation.enums.InternalStatus;

// This class represents the behaviour of exception handling in PaymentController before changes.
// Left as a default to not change all agents at once.
public class PaymentControllerOldExceptionMapper implements PaymentControllerAgentExceptionMapper {

    @Override
    public Optional<RuntimeException> mapToPaymentException(
            AgentException agentException, PaymentControllerAgentExceptionMapperContext context) {

        if (context == PaymentControllerAgentExceptionMapperContext.SIGN) {
            PaymentException newExc = null;
            if (agentException instanceof AuthenticationException) {
                AuthenticationException authException = (AuthenticationException) agentException;
                if (agentException instanceof BankIdException) {
                    BankIdError bankIdError = ((BankIdException) agentException).getError();
                    switch (bankIdError) {
                        case CANCELLED:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.CANCELLED,
                                            InternalStatus.BANKID_CANCELLED,
                                            authException);
                            break;
                        case NO_CLIENT:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.NO_CLIENT,
                                            InternalStatus.BANKID_NO_RESPONSE,
                                            authException);
                            break;
                        case TIMEOUT:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.TIMEOUT,
                                            InternalStatus.BANKID_TIMEOUT,
                                            authException);
                            break;
                        case INTERRUPTED:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.INTERRUPTED,
                                            InternalStatus.BANKID_INTERRUPTED,
                                            authException);
                            break;
                        case ACTIVATE_EXTENDED_BANKID:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.NO_EXTENDED_USE,
                                            InternalStatus.BANKID_NEEDS_EXTENDED_USE_ENABLED,
                                            authException);
                            break;
                        case UNKNOWN:
                        default:
                            newExc =
                                    new PaymentAuthorizationException(
                                            BankId.UNKNOWN,
                                            InternalStatus.BANKID_UNKNOWN_EXCEPTION,
                                            authException);
                    }
                } else {
                    newExc =
                            new PaymentAuthorizationException(
                                    "Payment could not be signed", agentException);
                }
            }
            return Optional.ofNullable(newExc);
        }
        return Optional.empty();
    }
}
