package se.tink.backend.aggregation.workers.commands;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;

@Slf4j
public class SetInitialAndFinalOperationStatusAgentWorkerCommand extends AgentWorkerCommand {

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
                    if (currentStatus.equals(OperationStatus.IMPOSSIBLE_TO_ABORT)) {
                        return OperationStatus.COMPLETED;
                    } else if (currentStatus.equals(OperationStatus.ABORTING)) {
                        return OperationStatus.ABORTED;
                    } else if (currentStatus.equals(OperationStatus.STARTED)
                            || currentStatus.equals(OperationStatus.TRYING_TO_ABORT)) {
                        log.warn(
                                "Status before completing is {}, but expected one of {}",
                                currentStatus,
                                Arrays.asList(
                                        OperationStatus.IMPOSSIBLE_TO_ABORT,
                                        OperationStatus.ABORTING));
                        return OperationStatus.COMPLETED;
                    } else {
                        log.error("Invalid status {}", currentStatus);
                        return currentStatus;
                    }
                }));
    }
}
