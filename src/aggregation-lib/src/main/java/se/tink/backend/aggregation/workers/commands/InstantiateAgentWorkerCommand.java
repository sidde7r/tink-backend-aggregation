package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.ServiceContext;

public class InstantiateAgentWorkerCommand extends AgentWorkerCommand {
    public static class InstantiateAgentWorkerCommandState {
        private AgentFactory agentFactory;

        public InstantiateAgentWorkerCommandState(ServiceContext serviceContext) {
            agentFactory = new AgentFactory(serviceContext.getConfiguration());
        }

        public AgentFactory getAgentFactory() {
            return agentFactory;
        }
    }

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
