package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;

public class InstantiateAgentWorkerCommand extends AgentWorkerCommand {

    private InstantiateAgentWorkerCommandState state;
    private AgentWorkerContext context;

    public InstantiateAgentWorkerCommand(AgentWorkerContext context, InstantiateAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        context.setAgent(state.getAgentFactory().create(context.getRequest(), context));

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
