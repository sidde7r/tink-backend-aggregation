package se.tink.backend.aggregation.workers.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class CredentialsStatusEventListener implements AgentEventListener {
    private final AgentWorkerCommandContext context;
    private final SignableOperation signableOperation;
    private final Credentials credentials;
    private static final Logger logger =
            LoggerFactory.getLogger(CredentialsStatusEventListener.class);

    public CredentialsStatusEventListener(
            AgentWorkerCommandContext context,
            Credentials credentials,
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
                context.updateSignableOperationStatus(
                        signableOperation,
                        SignableOperationStatuses.CANCELLED,
                        credentials.getStatusPayload());
                logger.info(
                        "Signable operation status is set to cancelled for credentials with status {}",
                        credentials.getStatus());
                break;
            case TEMPORARY_ERROR:
                context.updateSignableOperationStatus(
                        signableOperation,
                        SignableOperationStatuses.FAILED,
                        credentials.getStatusPayload());
                break;
            case AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
            case AWAITING_MOBILE_BANKID_AUTHENTICATION:
            case AWAITING_SUPPLEMENTAL_INFORMATION:
                context.updateSignableOperationStatus(
                        signableOperation, SignableOperationStatuses.AWAITING_CREDENTIALS, null);
                break;
            default:
                break;
        }
    }
}
