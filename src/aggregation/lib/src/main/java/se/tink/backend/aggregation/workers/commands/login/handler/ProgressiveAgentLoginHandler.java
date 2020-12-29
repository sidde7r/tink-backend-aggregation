package se.tink.backend.aggregation.workers.commands.login.handler;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ProgressiveAgentLoginHandler implements LoginHandler {

    @Override
    public Optional<LoginResult> handle(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        if (agent instanceof ProgressiveAuthAgent) {
            ProgressiveAuthAgent progressiveAgent = (ProgressiveAuthAgent) agent;
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, progressiveAgent);
            return Optional.of(
                    LegacyAgentLoginProcessor.create(
                            () -> {
                                executor.login(credentialsRequest);
                                return new LoginSuccessResult();
                            }));
        }
        return Optional.empty();
    }
}
