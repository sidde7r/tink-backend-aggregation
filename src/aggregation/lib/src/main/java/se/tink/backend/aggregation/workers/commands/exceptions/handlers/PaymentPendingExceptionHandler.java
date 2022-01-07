package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentPendingException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class PaymentPendingExceptionHandler implements ExceptionHandler<PaymentPendingException> {

    @Override
    public Class<PaymentPendingException> getSupportedExceptionClass() {
        return PaymentPendingException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            PaymentPendingException exception, ExceptionHandlerInput input) {
        input.getMetricAction().failed();

        log.warn(
                "[transferId: {}] Could not execute payment due to payment in PENDING state. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage(),
                exception);

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.CREATED);
        signableOperation.setStatusMessage(
                input.getCatalog()
                        .getString(
                                getStatusMessage(
                                        exception.getMessage(), PaymentPendingException.MESSAGE)));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
