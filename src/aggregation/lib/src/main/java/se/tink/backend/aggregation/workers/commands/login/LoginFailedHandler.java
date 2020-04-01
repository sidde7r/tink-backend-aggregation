package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class LoginFailedHandler implements LoginHandler {

    private static final AggregationLogger log = new AggregationLogger(LoginFailedHandler.class);

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            Agent agent, MetricActionIface metricAction, Credentials credentials) throws Exception {
        log.warn("Login failed due to agent.login() returned false");
        metricAction.failed();
        return Optional.of(AgentWorkerCommandResult.ABORT);
    }
}
