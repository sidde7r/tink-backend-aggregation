package se.tink.backend.aggregation.workers.commands;

import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RefreshCommandChainEventTriggerCommand extends AgentWorkerCommand {
    private final Credentials credentials;
    private final String appId;
    private final CredentialsEventProducer credentialsEventProducer;
    private final String correlationId;
    private final Set<RefreshableItem> refreshableItems;
    private final boolean manual;
    private final String clusterId;

    public RefreshCommandChainEventTriggerCommand(
            CredentialsEventProducer credentialsEventProducer,
            String correlationId,
            Credentials credentials,
            String appId,
            Set<RefreshableItem> refreshableItems,
            boolean manual,
            String clusterId) {
        this.credentialsEventProducer = credentialsEventProducer;
        this.credentials = credentials;
        this.appId = appId;
        this.correlationId = correlationId;
        this.refreshableItems = refreshableItems;
        this.manual = manual;
        this.clusterId = clusterId;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        credentialsEventProducer.sendCredentialsRefreshCommandChainStarted(
                credentials, appId, correlationId, clusterId, manual, refreshableItems);
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        credentialsEventProducer.sendCredentialsRefreshCommandChainFinished(
                credentials, appId, correlationId, clusterId, manual);
    }
}
