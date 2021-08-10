package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class PaymentExceptionHandler implements ExceptionHandler<PaymentException> {

    @Override
    public Class<PaymentException> getSupportedExceptionClass() {
        return PaymentException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            PaymentException exception, ExceptionHandlerInput input) {
        input.getMetricAction().failed();

        log.info(
                "[transferId: {}] Could not execute transfer due to Payment exception. {}",
                UUIDUtils.toTinkUUID(input.getTransfer().getId()),
                exception.getMessage());

        SignableOperation signableOperation = input.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.FAILED);
        signableOperation.setStatusMessage(input.getCatalog().getString("Payment failed."));
        signableOperation.setInternalStatus(exception.getInternalStatus());
        input.getContext().updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.ABORT;
    }
}
