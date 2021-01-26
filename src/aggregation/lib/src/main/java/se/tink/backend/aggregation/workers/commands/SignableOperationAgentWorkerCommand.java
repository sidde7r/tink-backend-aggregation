package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.listeners.CredentialsStatusEventListener;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

abstract class SignableOperationAgentWorkerCommand extends AgentWorkerCommand {
    protected final AgentWorkerCommandContext context;
    protected final StatusUpdater statusUpdater;
    protected final Credentials credentials;
    private final AgentEventListener credentialsStatusListener;

    SignableOperationAgentWorkerCommand(
            AgentWorkerCommandContext context,
            Credentials credentials,
            SignableOperation signableOperation) {
        this.context = context;
        this.statusUpdater = context;
        this.credentials = credentials;
        this.credentialsStatusListener =
                new CredentialsStatusEventListener(context, credentials, signableOperation);

        context.addEventListener(credentialsStatusListener);
    }

    void resetCredentialsStatus() {
        context.removeEventListener(credentialsStatusListener);

        CredentialsStatus credentialsStatus = credentials.getStatus();
        if (credentialsStatus.equals(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION)
                || credentialsStatus.equals(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION)) {
            statusUpdater.updateStatus(CredentialsStatus.UNCHANGED);
        }
    }
}
