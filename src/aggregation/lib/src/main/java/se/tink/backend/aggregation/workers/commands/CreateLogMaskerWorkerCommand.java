package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;

public class CreateLogMaskerWorkerCommand extends AgentWorkerCommand {

    private static final AggregationLogger log =
            new AggregationLogger(CreateAgentConfigurationControllerWorkerCommand.class);

    private final Credentials credentials;
    private final AgentWorkerCommandContext agentWorkerCommandContext;

    public CreateLogMaskerWorkerCommand(AgentWorkerCommandContext agentWorkerCommandContext) {
        this.credentials = agentWorkerCommandContext.getRequest().getCredentials();
        this.agentWorkerCommandContext = agentWorkerCommandContext;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        if (agentWorkerCommandContext.getAgentConfigurationController() == null) {
            log.error(
                    "No AgentConfigurationFound in CreatLogMaskerWorkerCommand, make sure to put the commands in the right order, this should come after the CreateAgentConfigurationControllerWorkerCommand.");
            return AgentWorkerCommandResult.ABORT;
        }

        LogMasker logMasker = new LogMasker(credentials);
        agentWorkerCommandContext.getAgentConfigurationController().addObserver(logMasker);
        agentWorkerCommandContext.setLogMasker(logMasker);

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {}
}
