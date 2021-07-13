package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class InstantiateAgentWorkerCommand extends AgentWorkerCommand {

    private InstantiateAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;

    public InstantiateAgentWorkerCommand(
            AgentWorkerCommandContext context, InstantiateAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        state.doRightBeforeInstantiation(
                context.getRequest().getProvider().getName(),
                context.getRequest().getCredentials().getId());
        context.setAgent(state.getAgentFactory().create(context.getRequest(), context));
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        state.doAtInstantiationPostProcess();
    }
}
