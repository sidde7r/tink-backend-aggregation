package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class PaymentRejectedExceptionHandler implements ExceptionHandler<PaymentRejectedException> {

    @Override
    public Class<PaymentRejectedException> getSupportedExceptionClass() {
        return PaymentRejectedException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            PaymentRejectedException exception, ExceptionHandlerInput input) {
        input.getMetricAction().failed();

        log.info(
                "[transferId: {}] Could not execute payment due to payment rejected by the bank. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage());

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.FAILED);
        signableOperation.setStatusMessage(
                input.getCatalog()
                        .getString(
                                getStatusMessage(
                                        exception.getMessage(), PaymentRejectedException.MESSAGE)));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
