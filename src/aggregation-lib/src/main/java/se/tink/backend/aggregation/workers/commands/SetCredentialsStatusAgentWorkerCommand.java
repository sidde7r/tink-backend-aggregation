package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;

public class SetCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {
    private StatusUpdater statusUpdater;
    private CredentialsStatus status;

    public SetCredentialsStatusAgentWorkerCommand(AgentWorkerCommandContext context, CredentialsStatus status) {
        this.statusUpdater = context;
        this.status = status;

    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        statusUpdater.updateStatus(status);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
