package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;

@Slf4j
public class SetImpossibleToAbortRequestStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final String requestId;
    private final RequestStatusManager statusManager;

    public SetImpossibleToAbortRequestStatusAgentWorkerCommand(
            String requestId, RequestStatusManager statusManager) {
        this.requestId = Objects.requireNonNull(requestId);
        this.statusManager = Objects.requireNonNull(statusManager);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        RequestStatus status = RequestStatus.IMPOSSIBLE_TO_ABORT;
        statusManager.set(requestId, status);
        log.info("Setting operation status for requestId: {}, status: {}", requestId, status);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {}
}
