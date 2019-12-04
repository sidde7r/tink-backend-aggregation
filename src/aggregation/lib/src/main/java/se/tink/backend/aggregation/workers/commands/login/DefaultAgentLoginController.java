package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class DefaultAgentLoginController implements LoginHandler {

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            Agent agent, MetricActionIface metricAction) throws Exception {
        if (agent.login()) {
            metricAction.completed();
            return Optional.of(AgentWorkerCommandResult.CONTINUE);
        }
        return Optional.empty();
    }
}
