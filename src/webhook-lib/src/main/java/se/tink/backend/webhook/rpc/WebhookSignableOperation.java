package se.tink.backend.webhook.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.firehose.v1.models.SignableOperation;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookSignableOperation {
    private static final ImmutableMap<SignableOperation.Status, SignableOperationStatuses> FIREHOSE_TO_CORE_SIGNABLE_OPERATION_STATUS_MAPPING =
            ImmutableMap.<SignableOperation.Status, SignableOperationStatuses>builder()
                    .put(SignableOperation.Status.STATUS_EXECUTING, SignableOperationStatuses.EXECUTING)
                    .put(SignableOperation.Status.STATUS_AWAITING_CREDENTIALS,
                            SignableOperationStatuses.AWAITING_CREDENTIALS)
                    .put(SignableOperation.Status.STATUS_CANCELLED, SignableOperationStatuses.CANCELLED)
                    .put(SignableOperation.Status.STATUS_FAILED, SignableOperationStatuses.FAILED)
                    .put(SignableOperation.Status.STATUS_EXECUTED, SignableOperationStatuses.EXECUTED)
                    .build();

    private static final ImmutableMap<SignableOperation.Type, SignableOperationTypes> FIREHOSE_TO_CORE_SIGNABLE_OPERATION_TYPE_MAPPING =
            ImmutableMap.<SignableOperation.Type, SignableOperationTypes>builder()
                    .put(SignableOperation.Type.TYPE_TRANSFER, SignableOperationTypes.TRANSFER)
                    .put(SignableOperation.Type.TYPE_ACCOUNT_CREATE, SignableOperationTypes.ACCOUNT_CREATE)
                    .put(SignableOperation.Type.TYPE_APPLICATION, SignableOperationTypes.APPLICATION)
                    .build();

    private long created;
    private long updated;
    private String id;
    private String status;
    private String statusMessage;
    private String type;
    private String underlyingId;
    private String userId;
    private String credentialsId;

    public static WebhookSignableOperation fromFirehoseSignableOperation(SignableOperation signableOperation) {
        if (signableOperation == null) {
            return null;
        }

        WebhookSignableOperation target = new WebhookSignableOperation();

        SignableOperationStatuses status = FIREHOSE_TO_CORE_SIGNABLE_OPERATION_STATUS_MAPPING.get(
                signableOperation.getStatus());
        String statusString = status == null ? null : status.name();

        SignableOperationTypes type = FIREHOSE_TO_CORE_SIGNABLE_OPERATION_TYPE_MAPPING.get(signableOperation.getType());
        String typeString = type == null ? null : type.name();

        target.created = signableOperation.getCreated();
        target.updated = signableOperation.getUpdated();
        target.id = signableOperation.getId();
        target.status = statusString;
        target.statusMessage = signableOperation.getStatusMessage();
        target.type = typeString;
        target.underlyingId = signableOperation.getUnderlyingId();
        target.userId = signableOperation.getUserId();
        target.credentialsId = signableOperation.getCredentialsId();
        return target;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getType() {
        return type;
    }

    public String getUnderlyingId() {
        return underlyingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }
}
