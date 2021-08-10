package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class TransferExecutionExceptionHandler
        implements ExceptionHandler<TransferExecutionException> {

    @Override
    public Class<TransferExecutionException> getSupportedExceptionClass() {
        return TransferExecutionException.class;
    }

    @Override
    public AgentWorkerCommandResult handleException(
            TransferExecutionException exception, ExceptionHandlerInput input) {
        // Catching this exception here means that the Credentials will not get status
        // TEMPORARY_ERROR.
        MetricAction metricAction = input.getMetricAction();
        Transfer transfer = input.getTransfer();
        if (Objects.equal(
                exception.getSignableOperationStatus(), SignableOperationStatuses.CANCELLED)) {
            // Skipping logging the exception, e, here because that will log stacktrace which we
            // will alert on
            // and register on dashboard as an error.

            metricAction.cancelled();
            log.info(
                    "[transferId: {}] Could not execute transfer. Transfer has been set CANCELLED due to {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    exception.getUserMessage());
        } else {
            metricAction.failed();
            log.error(
                    "[transferId: {}] Could not execute transfer.",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    exception);
        }

        input.getContext()
                .updateSignableOperationStatus(
                        input.getSignableOperation(),
                        exception.getSignableOperationStatus(),
                        exception.getUserMessage(),
                        exception.getInternalStatus());

        return AgentWorkerCommandResult.ABORT;
    }
}
