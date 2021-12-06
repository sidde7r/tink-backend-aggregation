package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class DuplicatePaymentExceptionHandler
        implements ExceptionHandler<DuplicatePaymentException> {

    @Override
    public Class<DuplicatePaymentException> getSupportedExceptionClass() {
        return DuplicatePaymentException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            DuplicatePaymentException exception, ExceptionHandlerInput input) {
        input.getMetricAction().cancelled();
        log.info(
                "[transferId: {}] Could not execute payment, duplicate payment detected. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage(),
                exception);

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
        signableOperation.setStatusMessage(
                input.getCatalog()
                        .getString(
                                getStatusMessage(
                                        exception.getMessage(),
                                        DuplicatePaymentException.DEFAULT_MESSAGE)));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);
        return AgentWorkerCommandResult.ABORT;
    }
}
