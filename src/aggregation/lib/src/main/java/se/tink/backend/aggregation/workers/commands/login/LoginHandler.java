package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface LoginHandler {

    Optional<AgentWorkerCommandResult> handleLogin(
            final Agent agent,
            final MetricActionIface metricAction,
            final CredentialsRequest credentials)
            throws Exception;
}
