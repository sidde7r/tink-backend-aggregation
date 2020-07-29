package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class KeepAliveAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger logger =
            new AggregationLogger(KeepAliveAgentWorkerCommand.class);
    private AgentWorkerCommandContext context;

    public KeepAliveAgentWorkerCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();
        Agent agent = context.getAgent();

        if (agent instanceof PersistentLogin) {
            PersistentLogin persistentAgent = (PersistentLogin) agent;

            // Load any persisted session data.

            persistentAgent.loadLoginSession();

            // Execute a keep-alive and see if the session is still valid.

            boolean alive = persistentAgent.keepAlive();

            logger.info(alive ? "Credential is alive" : "Credential is not alive");

            // If we're not alive, we might as well clear the persisted session data.

            if (!alive) {
                persistentAgent.clearLoginSession();
            }

            return AgentWorkerCommandResult.CONTINUE;
        } else {
            logger.info(
                    String.format(
                            "Agent %s does not implement PersistentLogin",
                            agent.getAgentClass().getName()));

            return AgentWorkerCommandResult.ABORT;
        }
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
