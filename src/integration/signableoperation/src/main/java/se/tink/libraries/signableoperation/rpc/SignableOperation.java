package se.tink.libraries.signableoperation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.UUID;
import se.tink.libraries.jersey.utils.SafelyLoggable;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.enums.SignableOperationTypes;
import se.tink.libraries.transfer.rpc.Transfer;

public class SignableOperation implements SafelyLoggable {
    private static final ImmutableSet<SignableOperationStatuses> IN_PROGRESS_STATUSES =
            ImmutableSet.of(
                    SignableOperationStatuses.EXECUTING,
                    SignableOperationStatuses.AWAITING_CREDENTIALS,
                    SignableOperationStatuses.CREATED);

    private Date created;
    private UUID id;
    private String status;
    private StatusDetailsKey statusDetailsKey;
    private String statusMessage;
    private String type;
    private UUID underlyingId;
    private Date updated;
    private UUID userId;
    private UUID credentialsId;
    private String signableObject;
    // This is an optional field to set a more fine-grained status when our external status are too
    // coarse. Internal use-only!
    // Do not expect this field to be always non-null. It is agent developers responsibility to
    // decide if internalStatus is helpful
    private String internalStatus;

    // Every aggregation operation has a unique correlationId, also saved in its events in BQ
    // adding correlationId to Signable operation will provide us a way to co-relate Transfer event
    // to each aggregation event, helping us tracing the root-cause of failed transfers,
    // which don't reach to transferAgentWorkerCommand
    private String correlationId;

    public SignableOperation() {}

    public SignableOperation(Transfer transfer) {
        this.underlyingId = transfer.getId();
        this.userId = transfer.getUserId();
        this.credentialsId = transfer.getCredentialsId();
        this.type = SignableOperationTypes.TRANSFER.name();
        this.signableObject = SerializationUtils.serializeToString(transfer);
    }

    public Date getCreated() {
        return created;
    }

    public UUID getId() {
        return id;
    }

    public SignableOperationStatuses getStatus() {
        if (status == null) {
            return null;
        }
        return SignableOperationStatuses.valueOf(status);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public StatusDetailsKey getStatusDetailsKey() {
        return statusDetailsKey;
    }

    public String getStatusMessage() {
        return Strings.isNullOrEmpty(statusMessage) ? getDefaultStatusMessage() : statusMessage;
    }

    private String getDefaultStatusMessage() {
        if (statusDetailsKey == null) {
            return null;
        }

        switch (statusDetailsKey) {
            case TECHNICAL_ERROR:
                return "Ett tekniskt fel uppstod, vänligen försök igen senare";
            case INVALID_INPUT:
                return "Ett eller flera felaktigt ifyllda fält, vänligen kontakta Tinks support: support@tink.se";
            case BANKID_FAILED:
                return "BankID signeringen misslyckades, vänligen försök igen";
            default:
                return null;
        }
    }

    public SignableOperationTypes getType() {
        if (type == null) {
            return null;
        }
        return SignableOperationTypes.valueOf(type);
    }

    public UUID getUnderlyingId() {
        return underlyingId;
    }

    public Date getUpdated() {
        return updated;
    }

    public UUID getUserId() {
        return userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSignableObject() {
        return signableObject;
    }

    public <T> T getSignableObject(Class<T> cls) {
        return SerializationUtils.deserializeFromString(signableObject, cls);
    }

    @JsonIgnore
    public boolean isInProgress() {
        return IN_PROGRESS_STATUSES.contains(getStatus());
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setStatus(SignableOperationStatuses status) {
        if (status == null) {
            this.status = null;
        } else {
            this.status = status.name();
        }
    }

    public void setStatusDetailsKey(StatusDetailsKey statusDetailsKey) {
        this.statusDetailsKey = statusDetailsKey;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setType(SignableOperationTypes type) {
        this.type = type.name();
    }

    public void setUnderlyingId(UUID underlyingId) {
        this.underlyingId = underlyingId;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setSignableObject(String signableObject) {
        this.signableObject = signableObject;
    }

    public void setSignableObject(Object object) {
        setSignableObject(SerializationUtils.serializeToString(object));
    }

    public String getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(String internalStatus) {
        this.internalStatus = internalStatus;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public enum StatusDetailsKey {
        BANKID_FAILED,
        INVALID_INPUT,
        TECHNICAL_ERROR,
        USER_VALIDATION_ERROR
    }

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("created", created)
                .add("id", id)
                .add("status", status)
                .add("statusDetailsKey", statusDetailsKey)
                .add("statusMessage", statusMessage)
                .add("type", type)
                .add("underlyingId", underlyingId)
                .add("updated", updated)
                .add("userId", userId)
                .add("credentialsId", credentialsId)
                .add("signableObject", signableObject == null ? null : "***")
                .add("internalStatus", internalStatus)
                .add("correlationId", correlationId)
                .toString();
    }
}
