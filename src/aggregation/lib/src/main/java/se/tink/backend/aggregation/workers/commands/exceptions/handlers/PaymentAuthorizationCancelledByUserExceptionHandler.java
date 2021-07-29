package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class PaymentAuthorizationCancelledByUserExceptionHandler
        implements ExceptionHandler<PaymentAuthorizationCancelledByUserException> {

    @Override
    public Class<PaymentAuthorizationCancelledByUserException> getSupportedExceptionClass() {
        return PaymentAuthorizationCancelledByUserException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            PaymentAuthorizationCancelledByUserException exception, ExceptionHandlerInput input) {
        input.getMetricAction().cancelled();

        log.info(
                "[transferId: {}] Could not execute payment due to authorisation cancelled by user.. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage());

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
        signableOperation.setStatusMessage(
                input.getCatalog()
                        .getString(
                                getStatusMessage(
                                        exception.getMessage(),
                                        PaymentAuthorizationCancelledByUserException.MESSAGE)));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
