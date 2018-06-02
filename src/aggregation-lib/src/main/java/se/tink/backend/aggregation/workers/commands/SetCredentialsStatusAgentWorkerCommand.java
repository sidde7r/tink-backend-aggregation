package se.tink.backend.aggregation.workers.commands;

import java.util.function.Predicate;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;

public class SetCredentialsStatusAgentWorkerCommand extends AgentWorkerCommand {

    private final Predicate<AgentWorkerContext> predicate;
    private AgentWorkerContext context;
    private CredentialsStatus status;

    public SetCredentialsStatusAgentWorkerCommand(AgentWorkerContext context, CredentialsStatus status) {
        this.context = context;
        this.status = status;
        this.predicate = x -> true;
    }

    /**
     * Include an additional predicate that needs to be fulfilled for the status to be updated.
     */
    public SetCredentialsStatusAgentWorkerCommand(AgentWorkerContext context, CredentialsStatus status,
            Predicate<AgentWorkerContext> predicate) {
        this.context = context;
        this.status = status;
        this.predicate = predicate;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (predicate.test(context)) {
            context.updateStatus(status);
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
