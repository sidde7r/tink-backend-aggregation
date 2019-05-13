package se.tink.backend.aggregation.workers.listeners;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class CredentialsStatusEventListener implements AgentEventListener {
    private final AgentWorkerCommandContext context;
    private final SignableOperation signableOperation;
    private final Credentials credentials;

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
                        credentials.getStatusPayload(),
                        null);
                break;
            case TEMPORARY_ERROR:
                context.updateSignableOperationStatus(
                        signableOperation,
                        SignableOperationStatuses.FAILED,
                        credentials.getStatusPayload(),
                        null);
                break;
            case AWAITING_THIRD_PARTY_APP_AUTHENTICATION:
            case AWAITING_MOBILE_BANKID_AUTHENTICATION:
            case AWAITING_SUPPLEMENTAL_INFORMATION:
                String supplementalInfo = credentials.getSupplementalInformation();

                context.updateSignableOperationStatus(
                        signableOperation,
                        SignableOperationStatuses.AWAITING_CREDENTIALS,
                        null,
                        supplementalInfo);
                break;
            default:
                break;
        }
    }
}
