package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;

@Slf4j
public class SetImpossibleToAbortOperationStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final String operationId;
    private final OperationStatusManager statusManager;

    public SetImpossibleToAbortOperationStatusAgentWorkerCommand(
            String operationId, OperationStatusManager statusManager) {
        this.operationId = Objects.requireNonNull(operationId);
        this.statusManager = Objects.requireNonNull(statusManager);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        OperationStatus status = OperationStatus.IMPOSSIBLE_TO_ABORT;
        statusManager.set(operationId, status);
        log.info("Setting operation status for operationId: {}, status: {}", operationId, status);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {}
}
