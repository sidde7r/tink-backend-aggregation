package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ProgressiveAuthenticatorLoginHandler implements LoginHandler {

    private SupplementalInformationController supplementalInformationController;

    public ProgressiveAuthenticatorLoginHandler(
            SupplementalInformationController supplementalInformationController) {
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            final Agent agent,
            final MetricActionIface metricAction,
            CredentialsRequest credentialsRequest)
            throws Exception {
        if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login(credentialsRequest);
            metricAction.completed();
            return Optional.of(AgentWorkerCommandResult.CONTINUE);
        }
        return Optional.empty();
    }
}
