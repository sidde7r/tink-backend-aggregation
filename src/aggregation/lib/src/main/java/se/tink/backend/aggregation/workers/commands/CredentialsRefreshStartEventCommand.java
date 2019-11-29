package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.uuid.UUIDUtils;

public class CredentialsRefreshStartEventCommand extends AgentWorkerCommand {
    private final Credentials credentials;
    private final String appId;
    private final CredentialsEventProducer credentialsEventProducer;
    private final String correlationId;

    public CredentialsRefreshStartEventCommand(
            CredentialsEventProducer credentialsEventProducer,
            Credentials credentials,
            String appId) {
        this.credentialsEventProducer = credentialsEventProducer;
        this.credentials = credentials;
        this.appId = appId;
        this.correlationId = UUIDUtils.generateUUID();
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        credentialsEventProducer.sendCredentialsRefreshCommandChainStarted(
                credentials, appId, correlationId);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        credentialsEventProducer.sendCredentialsRefreshCommandChainFinished(
                credentials, appId, correlationId);
    }
}
