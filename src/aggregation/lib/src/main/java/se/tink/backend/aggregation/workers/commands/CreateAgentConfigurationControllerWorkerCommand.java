package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClient;

public class CreateAgentConfigurationControllerWorkerCommand extends AgentWorkerCommand {

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final AgentWorkerCommandContext agentWorkerCommandContext;
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private AgentConfigurationController agentConfigurationController;

    public CreateAgentConfigurationControllerWorkerCommand(
            AgentWorkerCommandContext agentWorkerCommandContext,
            TppSecretsServiceClient tppSecretsServiceClient) {
        this.agentsServiceConfiguration = agentWorkerCommandContext.getAgentsServiceConfiguration();
        this.agentWorkerCommandContext = agentWorkerCommandContext;
        this.tppSecretsServiceClient = tppSecretsServiceClient;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        agentConfigurationController =
                new AgentConfigurationController(
                        tppSecretsServiceClient,
                        agentsServiceConfiguration.getIntegrations(),
                        agentWorkerCommandContext.getRequest().getProvider(),
                        agentWorkerCommandContext.getRequest().getCredentials(),
                        agentWorkerCommandContext.getAppId(),
                        agentWorkerCommandContext.getClusterId(),
                        agentWorkerCommandContext.getRequest().getCallbackUri());

        agentWorkerCommandContext.setAgentConfigurationController(agentConfigurationController);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        Optional.ofNullable(agentConfigurationController)
                .ifPresent(AgentConfigurationController::completeSecretValuesSubject);
    }
}
