package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class KeepAliveAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log =
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

            log.info(alive ? "Credential is alive" : "Credential is not alive");

            // If we're not alive, we might as well clear the persisted session data.

            if (!alive) {
                persistentAgent.clearLoginSession();
            }

            return AgentWorkerCommandResult.CONTINUE;
        } else {
            log.info(
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
