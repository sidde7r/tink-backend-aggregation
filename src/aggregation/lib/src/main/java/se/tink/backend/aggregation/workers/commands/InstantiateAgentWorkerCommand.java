package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class InstantiateAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(InstantiateAgentWorkerCommand.class);
    private static final double TEST_RATIO = 0.1; // 10%
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

        // TESTING NEW PATH WORKS

        log.info(
                "[TESTING NEW INSTANTIATE AGENT] picking new instantiate agent path to test it works");

        AgentInstance agentInstance =
                state.getAgentFactory().createAgentSdkInstance(context.getRequest(), context);
        Optional<Agent> agent = agentInstance.instanceOf(Agent.class);
        if (agent.isPresent()) {
            log.info("[TESTING NEW INSTANTIATE AGENT] instantiation works!");
            context.setAgent(agent.get());
        } else {
            // Fallback
            log.info("[TESTING NEW INSTANTIATE AGENT] it did not work, let's fallback!");
            createAndAddAgentOldWay();
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
