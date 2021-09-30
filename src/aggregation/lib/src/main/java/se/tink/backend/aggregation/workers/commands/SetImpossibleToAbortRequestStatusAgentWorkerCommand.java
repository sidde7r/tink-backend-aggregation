package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;

@Slf4j
public class SetImpossibleToAbortRequestStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final String credentialsId;
    private final RequestStatusManager statusManager;

    public SetImpossibleToAbortRequestStatusAgentWorkerCommand(
            String credentialsId, RequestStatusManager statusManager) {
        this.credentialsId = Objects.requireNonNull(credentialsId);
        this.statusManager = Objects.requireNonNull(statusManager);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        RequestStatus status = RequestStatus.IMPOSSIBLE_TO_ABORT;
        statusManager.setByCredentialsId(credentialsId, status);
        log.info(
                "Setting operation status for credentialsId: {}, status: {}",
                credentialsId,
                status);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {}
}
