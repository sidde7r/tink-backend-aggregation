package se.tink.backend.aggregation.workers.commands;

import java.util.Random;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class InstantiateAgentWorkerCommand extends AgentWorkerCommand {

    private static final double TEST_RATIO = 0.0; 
    private InstantiateAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;
    private final Random random;

    public InstantiateAgentWorkerCommand(
            AgentWorkerCommandContext context, InstantiateAgentWorkerCommandState state) {
        this.context = context;
        this.state = state;
        this.random = new Random();
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        state.doRightBeforeInstantiation(
                context.getRequest().getProvider().getName(),
                context.getRequest().getCredentials().getId());

        if (random.nextDouble() >= TEST_RATIO) {
            // REGULAR PATH
            createAndAddAgentOldWay();
            return AgentWorkerCommandResult.CONTINUE;
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        state.doAtInstantiationPostProcess();
    }

    private void createAndAddAgentOldWay() throws ReflectiveOperationException {
        Agent agent = state.getAgentFactory().create(context.getRequest(), context);
        context.setAgent(agent);
    }
}
