package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;

public class CreateAgentConfigurationControllerWorkerCommand extends AgentWorkerCommand {

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final AgentWorkerCommandContext agentWorkerCommandContext;
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final SecretsServiceInternalClient secretsServiceInternalClient;
    private AgentConfigurationControllerable agentConfigurationController;

    public CreateAgentConfigurationControllerWorkerCommand(
            AgentWorkerCommandContext agentWorkerCommandContext,
            TppSecretsServiceClient tppSecretsServiceClient,
            SecretsServiceInternalClient secretsServiceInternalClient) {
        this.agentsServiceConfiguration = agentWorkerCommandContext.getAgentsServiceConfiguration();
        this.agentWorkerCommandContext = agentWorkerCommandContext;
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.secretsServiceInternalClient = secretsServiceInternalClient;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        agentConfigurationController =
                new AgentConfigurationController(
                        tppSecretsServiceClient,
                        secretsServiceInternalClient,
                        agentsServiceConfiguration.getIntegrations(),
                        agentWorkerCommandContext.getRequest().getProvider(),
                        agentWorkerCommandContext.getAppId(),
                        agentWorkerCommandContext.getClusterId(),
                        agentWorkerCommandContext.getCertId(),
                        agentWorkerCommandContext.getRequest().getCallbackUri());

        agentWorkerCommandContext.setAgentConfigurationController(agentConfigurationController);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        Optional.ofNullable(agentConfigurationController)
                .ifPresent(AgentConfigurationControllerable::completeSecretValuesSubject);
    }
}
