package se.tink.backend.aggregation.workers.commands.login.handler;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface LoginHandler {

    Optional<LoginResult> handle(
            final Agent agent,
            final CredentialsRequest credentialsRequest,
            final SupplementalInformationController supplementalInformationController);
}
