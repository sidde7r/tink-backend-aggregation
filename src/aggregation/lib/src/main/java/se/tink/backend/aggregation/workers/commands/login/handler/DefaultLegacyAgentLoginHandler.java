package se.tink.backend.aggregation.workers.commands.login.handler;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DefaultLegacyAgentLoginHandler implements LoginHandler {
    @Override
    public Optional<LoginResult> handle(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        return Optional.of(
                LegacyAgentLoginProcessor.create(
                        () ->
                                agent.login()
                                        ? new LoginSuccessResult()
                                        : new LoginUnknownErrorResult(null)));
    }
}
