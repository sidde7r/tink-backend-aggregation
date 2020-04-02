package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public interface LoginHandler {

    Optional<AgentWorkerCommandResult> handleLogin(
            final Agent agent, final MetricActionIface metricAction, final Credentials credentials)
            throws Exception;
}
