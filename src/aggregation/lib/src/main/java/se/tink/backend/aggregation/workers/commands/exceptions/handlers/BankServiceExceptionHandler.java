package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class BankServiceExceptionHandler implements ExceptionHandler<BankServiceException> {

    @Override
    public Class<BankServiceException> getSupportedExceptionClass() {
        return BankServiceException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            BankServiceException exception, ExceptionHandlerInput input) {
        input.getMetricAction().unavailable();

        log.info(
                "[transferId: {}] Could not execute transfer due to bank side failure. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage());

        InternalStatus internalStatus =
                exception.getError() == BankServiceError.ACCESS_EXCEEDED
                        ? InternalStatus.RATE_LIMIT_EXCEEDED
                        : InternalStatus.BANK_SERVICE_UNAVAILABLE;

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.FAILED);
        signableOperation.setInternalStatus(internalStatus.toString());
        signableOperation.setStatusMessage(
                input.getCatalog().getString(exception.getUserMessage()));
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
