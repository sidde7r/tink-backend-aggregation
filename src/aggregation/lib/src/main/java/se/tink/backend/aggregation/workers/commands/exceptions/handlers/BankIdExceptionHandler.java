package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class BankIdExceptionHandler implements ExceptionHandler<BankIdException> {

    @Override
    public Class<BankIdException> getSupportedExceptionClass() {
        return BankIdException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            BankIdException exception, ExceptionHandlerInput input) {
        MetricAction metricAction = input.getMetricAction();
        Transfer transfer = input.getTransfer();
        SignableOperation signableOperation = input.getSignableOperation();
        switch (exception.getError()) {
            case CANCELLED:
            case TIMEOUT:
            case ALREADY_IN_PROGRESS:
            case NO_CLIENT:
            case AUTHORIZATION_REQUIRED: // TODO: This should be a regular
                // AuthorizationException
                metricAction.cancelled();
                log.info(
                        "[transferId: {}] {}",
                        UUIDUtils.toTinkUUID(transfer.getId()),
                        exception.getMessage());
                signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
                break;
            default:
                metricAction.failed();
                log.error(
                        "[transferId: {}] Caught unexpected {}",
                        UUIDUtils.toTinkUUID(transfer.getId()),
                        exception.getMessage(),
                        exception);
                signableOperation.setStatus(SignableOperationStatuses.FAILED);
        }

        signableOperation.setStatusMessage(
                input.getCatalog().getString(exception.getUserMessage()));
        signableOperation.setInternalStatus("BankId/" + exception.getError().name());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
