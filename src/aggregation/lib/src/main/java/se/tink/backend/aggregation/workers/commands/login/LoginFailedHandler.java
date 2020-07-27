package se.tink.backend.aggregation.workers.commands.login;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LoginFailedHandler implements LoginHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            Agent agent, MetricActionIface metricAction, CredentialsRequest credentialsRequest)
            throws Exception {
        logger.warn("Login failed due to agent.login() returned false");
        metricAction.failed();
        return Optional.of(AgentWorkerCommandResult.ABORT);
    }
}
