package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class DefaultExceptionHandler implements ExceptionHandler<Exception> {

    @Override
    public Class<Exception> getSupportedExceptionClass() {
        return Exception.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            Exception exception, ExceptionHandlerInput input) {
        // Errors down the line on Agent and we want to know, log and Fail transfer if
        // some thing is broken in any Agent

        // Catching this exception here means that the Credentials will not get status
        // TEMPORARY_ERROR.
        input.getMetricAction().failed();
        log.error(
                "[transferId: {}] Could not execute transfer. Something is badly broken",
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
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
