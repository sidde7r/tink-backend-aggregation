package se.tink.backend.aggregation.workers.commands;

import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;

public class SetCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final Predicate<AgentWorkerCommandContext> predicate;
    private AgentWorkerCommandContext context;
    private StatusUpdater statusUpdater;
    private CredentialsStatus status;

    public SetCredentialsStatusAgentWorkerCommand(AgentWorkerCommandContext context, CredentialsStatus status) {
        this.context = context;
        this.statusUpdater = context;
        this.status = status;
        this.predicate = x -> true;
    }

    /**
     * Include an additional predicate that needs to be fulfilled for the status to be updated.
     */
    public SetCredentialsStatusAgentWorkerCommand(AgentWorkerCommandContext context, CredentialsStatus status,
            Predicate<AgentWorkerCommandContext> predicate) {
        this.context = context;
        this.status = status;
        this.predicate = predicate;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (predicate.test(context)) {
            statusUpdater.updateStatus(status);
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
