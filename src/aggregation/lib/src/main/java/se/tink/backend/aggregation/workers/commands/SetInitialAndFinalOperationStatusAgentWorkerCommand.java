package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;

public class SetInitialAndFinalOperationStatusAgentWorkerCommand extends AgentWorkerCommand {

    private static final Logger logger =
            LoggerFactory.getLogger(SetInitialAndFinalOperationStatusAgentWorkerCommand.class);

    private final String operationId;
    private final OperationStatusManager statusManager;

    public SetInitialAndFinalOperationStatusAgentWorkerCommand(
            String operationId, OperationStatusManager statusManager) {
        this.operationId = Objects.requireNonNull(operationId);
        this.statusManager = Objects.requireNonNull(statusManager);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        statusManager.set(operationId, OperationStatus.STARTED);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        statusManager.compareAndSet(
                operationId,
                (currentStatus -> {
                    if (currentStatus.equals(OperationStatus.STARTED)
                            || currentStatus.equals(OperationStatus.TRYING_TO_ABORT)) {
                        return OperationStatus.COMPLETED;
                    } else if (currentStatus.equals(OperationStatus.ABORTING)) {
                        return OperationStatus.ABORTED;
                    } else {
                        logger.error(
                                "Invalid status: In doPostProcess the current status is {}",
                                currentStatus);
                        return currentStatus;
                    }
                }));
    }
}
