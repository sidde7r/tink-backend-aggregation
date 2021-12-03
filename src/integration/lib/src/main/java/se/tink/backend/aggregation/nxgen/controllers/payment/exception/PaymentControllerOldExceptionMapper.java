package se.tink.backend.aggregation.nxgen.controllers.payment.exception;

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
    public Optional<PaymentException> tryToMapToPaymentException(
            AgentException agentException, PaymentControllerAgentExceptionMapperContext context) {
        if (context == PaymentControllerAgentExceptionMapperContext.SIGN) {
            PaymentException paymentException = null;
            if (agentException instanceof AuthenticationException) {
                if (agentException instanceof BankIdException) {
                    paymentException = mapBankIdException((BankIdException) agentException);
                } else {
                    paymentException =
                            new PaymentAuthorizationException(
                                    "Payment could not be signed", agentException);
                }
            }
            return Optional.ofNullable(paymentException);
        }
        return Optional.empty();
    }

    static PaymentException mapBankIdException(BankIdException bankIdException) {
        BankIdError bankIdError = bankIdException.getError();
        switch (bankIdError) {
            case CANCELLED:
                return new PaymentAuthorizationException(
                        BankId.CANCELLED, InternalStatus.BANKID_CANCELLED, bankIdException);
            case NO_CLIENT:
                return new PaymentAuthorizationException(
                        BankId.NO_CLIENT, InternalStatus.BANKID_NO_RESPONSE, bankIdException);

            case TIMEOUT:
                return new PaymentAuthorizationException(
                        BankId.TIMEOUT, InternalStatus.BANKID_TIMEOUT, bankIdException);
            case INTERRUPTED:
                return new PaymentAuthorizationException(
                        BankId.INTERRUPTED, InternalStatus.BANKID_INTERRUPTED, bankIdException);
            case ACTIVATE_EXTENDED_BANKID:
                return new PaymentAuthorizationException(
                        BankId.NO_EXTENDED_USE,
                        InternalStatus.BANKID_NEEDS_EXTENDED_USE_ENABLED,
                        bankIdException);
            case UNKNOWN:
            default:
                return new PaymentAuthorizationException(
                        BankId.UNKNOWN, InternalStatus.BANKID_UNKNOWN_EXCEPTION, bankIdException);
        }
    }
}
