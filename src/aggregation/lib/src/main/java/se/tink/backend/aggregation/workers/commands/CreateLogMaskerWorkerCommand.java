package se.tink.backend.aggregation.workers.commands;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;

public class CreateLogMaskerWorkerCommand extends AgentWorkerCommand {
    private final Credentials credentials;
    private final AgentWorkerCommandContext agentWorkerCommandContext;
    private LogMasker logMasker;

    public CreateLogMaskerWorkerCommand(AgentWorkerCommandContext agentWorkerCommandContext) {
        this.credentials = agentWorkerCommandContext.getRequest().getCredentials();
        this.agentWorkerCommandContext = agentWorkerCommandContext;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (agentWorkerCommandContext.getAgentConfigurationController() == null) {
            throw new IllegalStateException(
                    "No AgentConfigurationController found in CreateLogMaskerWorkerCommand, make sure to put the commands in the right order, this should come after the CreateAgentConfigurationControllerWorkerCommand.");
        }

        logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();
        logMasker.addSensitiveValuesSetObservable(
                agentWorkerCommandContext
                        .getAgentConfigurationController()
                        .getSecretValuesObservable());
        agentWorkerCommandContext.setLogMasker(logMasker);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        Optional.ofNullable(logMasker).ifPresent(LogMasker::disposeOfAllSubscriptions);
    }
}
