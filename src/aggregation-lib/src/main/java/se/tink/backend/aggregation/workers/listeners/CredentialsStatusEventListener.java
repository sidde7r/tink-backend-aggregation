package se.tink.backend.aggregation.workers.listeners;

import se.tink.backend.aggregation.workers.AgentWorkerContext;

import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;

public class CredentialsStatusEventListener implements AgentEventListener {
    private final AgentWorkerContext context;
    private final SignableOperation signableOperation;
    private final Credentials credentials;

    public CredentialsStatusEventListener(AgentWorkerContext context, Credentials credentials,
            SignableOperation signableOperation) {
        this.context = context;
        this.credentials = credentials;
        this.signableOperation = signableOperation;
    }

    @Override
    public void onUpdateCredentialsStatus() {
        switch (credentials.getStatus()) {
        case UNCHANGED:
        case AUTHENTICATION_ERROR:
            context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.CANCELLED,
                    credentials.getStatusPayload());
            break;
        case TEMPORARY_ERROR:
            context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                    credentials.getStatusPayload());
            break;
        case AWAITING_MOBILE_BANKID_AUTHENTICATION:
        case AWAITING_SUPPLEMENTAL_INFORMATION:
            context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.AWAITING_CREDENTIALS);
            break;
        default:
            break;
        }
    }
}