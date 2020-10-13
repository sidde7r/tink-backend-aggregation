package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AllArgsConstructor
public class AgentPlatformAuthenticatorLoginHandler implements LoginHandler {

    private final SupplementalInformationController supplementalInformationController;

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            Agent agent, MetricActionIface metricAction, CredentialsRequest credentialsRequest)
            throws Exception {
        if (agent instanceof AgentPlatformAuthenticator) {
            AgentPlatformAuthenticationExecutor.processAuthentication(
                    agent, credentialsRequest, supplementalInformationController);
            return Optional.of(AgentWorkerCommandResult.CONTINUE);
        }
        return Optional.empty();
    }
}
