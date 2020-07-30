package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DefaultAgentLoginController implements LoginHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultAgentLoginController.class);

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            Agent agent, MetricActionIface metricAction, CredentialsRequest credentials)
            throws Exception {
        log.info(
                "DefaultAgentLoginController for credentials: {}",
                Optional.ofNullable(credentials.getCredentials())
                        .map(Credentials::getId)
                        .orElse(null));
        if (agent.login()) {
            metricAction.completed();
            return Optional.of(AgentWorkerCommandResult.CONTINUE);
        }
        return Optional.empty();
    }
}
