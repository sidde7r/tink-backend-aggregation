package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class ProgressiveAuthenticatorLoginHandler implements LoginHandler {

    private SupplementalInformationController supplementalInformationController;

    public ProgressiveAuthenticatorLoginHandler(
            SupplementalInformationController supplementalInformationController) {
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public Optional<AgentWorkerCommandResult> handleLogin(
            final Agent agent, final MetricActionIface metricAction) throws Exception {
        if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login();
            metricAction.completed();
            return Optional.of(AgentWorkerCommandResult.CONTINUE);
        }
        return Optional.empty();
    }
}
