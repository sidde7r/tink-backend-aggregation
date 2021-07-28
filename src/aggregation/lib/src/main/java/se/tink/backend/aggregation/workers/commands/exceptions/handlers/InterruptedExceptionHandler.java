package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class InterruptedExceptionHandler implements ExceptionHandler<InterruptedException> {

    @Override
    public Class<InterruptedException> getSupportedExceptionClass() {
        return InterruptedException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            InterruptedException exception, ExceptionHandlerInput input) {
        Thread.currentThread().interrupt();

        input.getMetricAction().failed();
        log.error(
                "[transferId: {}] Could not execute transfer.",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception);

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.FAILED);
        signableOperation.setStatusMessage(
                input.getCatalog()
                        .getString(
                                TransferExecutionException.EndUserMessage
                                        .GENERIC_PAYMENT_ERROR_MESSAGE
                                        .getKey()
                                        .get()));
        signableOperation.setInternalStatus(InternalStatus.BANK_CONNECTION_INTERRUPTED.toString());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
