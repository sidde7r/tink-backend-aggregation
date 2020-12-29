package se.tink.backend.aggregation.workers.commands.login.handler;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AllArgsConstructor
public class AgentPlatformAgentLoginHandler implements LoginHandler {

    private final AgentPlatformAuthenticationExecutor agentPlatformAuthenticationExecutor;

    @Override
    public Optional<LoginResult> handle(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        if (agent instanceof AgentPlatformAuthenticator) {
            try {
                agentPlatformAuthenticationExecutor.processAuthentication(
                        agent, credentialsRequest, supplementalInformationController);
                return Optional.of(new LoginSuccessResult());
            } catch (
                    AgentPlatformAuthenticationProcessException
                            agentPlatformAuthenticationProcessException) {
                return Optional.of(
                        new AgentPlatformLoginErrorResult(
                                agentPlatformAuthenticationProcessException));
            } catch (Exception ex) {
                return Optional.of(new LoginUnknownErrorResult(ex));
            }
        }
        return Optional.empty();
    }
}
