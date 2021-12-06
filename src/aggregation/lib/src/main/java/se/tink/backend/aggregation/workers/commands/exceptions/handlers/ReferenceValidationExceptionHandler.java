package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class ReferenceValidationExceptionHandler
        implements ExceptionHandler<ReferenceValidationException> {

    @Override
    public Class<ReferenceValidationException> getSupportedExceptionClass() {
        return ReferenceValidationException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            ReferenceValidationException exception, ExceptionHandlerInput input) {
        input.getMetricAction().cancelled();

        log.info(
                "[transferId: {}] Could not execute payment due to reference validation failure. {}",
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
                                        ReferenceValidationException.DEFAULT_MESSAGE)));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
