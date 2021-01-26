package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class SetCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {
    private StatusUpdater statusUpdater;
    private CredentialsStatus status;

    public SetCredentialsStatusAgentWorkerCommand(
            AgentWorkerCommandContext context, CredentialsStatus status) {
        this.statusUpdater = context;
        this.status = status;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        statusUpdater.updateStatus(status);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }
}
