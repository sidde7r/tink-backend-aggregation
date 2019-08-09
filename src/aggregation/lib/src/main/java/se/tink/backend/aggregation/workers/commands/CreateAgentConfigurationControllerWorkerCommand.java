package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreateAgentConfigurationControllerWorkerCommand extends AgentWorkerCommand {

    private static final AggregationLogger log =
            new AggregationLogger(CreateAgentConfigurationControllerWorkerCommand.class);

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final CredentialsRequest credentialsRequest;
    private final AgentWorkerCommandContext agentWorkerCommandContext;
    private final boolean tppSecretsServiceEnabled;

    public CreateAgentConfigurationControllerWorkerCommand(
            AgentWorkerCommandContext agentWorkerCommandContext) {
        this.agentsServiceConfiguration = agentWorkerCommandContext.getAgentsServiceConfiguration();
        this.credentialsRequest = agentWorkerCommandContext.getRequest();
        this.agentWorkerCommandContext = agentWorkerCommandContext;
        this.tppSecretsServiceEnabled =
                agentWorkerCommandContext
                        .getAgentsServiceConfiguration()
                        .getTppSecretsServiceConfiguration()
                        .isEnabled();
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        AgentConfigurationController agentConfigurationController =
                new AgentConfigurationController(
                        agentsServiceConfiguration.getTppSecretsServiceConfiguration(),
                        agentsServiceConfiguration.getIntegrations(),
                        credentialsRequest.getProvider().getFinancialInstitutionId(),
                        agentWorkerCommandContext.getAppId());

        agentWorkerCommandContext.setAgentConfigurationController(agentConfigurationController);

        if (!agentConfigurationController.init()) {
            log.warn("AgentConfigurationController could not be initialized.");
            return AgentWorkerCommandResult.ABORT;
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        Optional.ofNullable(agentWorkerCommandContext.getAgentConfigurationController())
                .ifPresent(
                        agentConfigurationController ->
                                agentConfigurationController.shutdownTppSecretsServiceClient());
    }
}
