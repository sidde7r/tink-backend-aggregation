package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.listeners.CredentialsStatusEventListener;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;

abstract class SignableOperationAgentWorkerCommand extends AgentWorkerCommand {
    protected final AgentWorkerContext context;
    protected final Credentials credentials;
    private final AgentEventListener credentialsStatusListener;

    SignableOperationAgentWorkerCommand(AgentWorkerContext context,
            Credentials credentials, SignableOperation signableOperation) {
        this.context = context;
        this.credentials = credentials;
        this.credentialsStatusListener = new CredentialsStatusEventListener(context,
                credentials, signableOperation);

        context.addEventListener(credentialsStatusListener);
    }

    void resetCredentialsStatus() {
        context.removeEventListener(credentialsStatusListener);

        CredentialsStatus credentialsStatus = credentials.getStatus();
        if (credentialsStatus.equals(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION) ||
                credentialsStatus.equals(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION)) {
            context.updateStatus(CredentialsStatus.UNCHANGED);
        }
    }
}
