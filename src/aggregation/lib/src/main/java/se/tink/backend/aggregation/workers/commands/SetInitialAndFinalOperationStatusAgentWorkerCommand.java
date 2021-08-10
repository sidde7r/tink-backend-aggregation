package se.tink.backend.aggregation.workers.commands;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;

@Slf4j
public class SetInitialAndFinalOperationStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final String requestId;
    private final RequestStatusManager statusManager;

    public SetInitialAndFinalOperationStatusAgentWorkerCommand(
            String requestId, RequestStatusManager statusManager) {
        this.requestId = Objects.requireNonNull(requestId);
        this.statusManager = Objects.requireNonNull(statusManager);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        statusManager.set(requestId, RequestStatus.STARTED);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        statusManager.compareAndSet(
                requestId,
                (currentStatus -> {
                    if (currentStatus.equals(RequestStatus.IMPOSSIBLE_TO_ABORT)) {
                        return RequestStatus.COMPLETED;
                    } else if (currentStatus.equals(RequestStatus.ABORTING)) {
                        return RequestStatus.ABORTED;
                    } else if (currentStatus.equals(RequestStatus.STARTED)
                            || currentStatus.equals(RequestStatus.TRYING_TO_ABORT)) {
                        log.warn(
                                "Status before completing is {}, but expected one of {}",
                                currentStatus,
                                Arrays.asList(
                                        RequestStatus.IMPOSSIBLE_TO_ABORT, RequestStatus.ABORTING));
                        return RequestStatus.COMPLETED;
                    } else {
                        log.error("Invalid status {}", currentStatus);
                        return currentStatus;
                    }
                }));
    }
}
