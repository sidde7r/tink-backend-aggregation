package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.core.enums.RefreshType;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialsEvent {
    private UUID userId;
    private UUID credentialsId;
    private UUID id;
    private String message;
    private String providerName;
    private String status;
    private Date timestamp;
    private String refreshType;

    public CredentialsEvent() {
    }

    public CredentialsEvent(Credentials credentials, CredentialsStatus status, String message, boolean isManual) {
        this.userId = UUIDUtils.fromTinkUUID(credentials.getUserId());
        this.credentialsId = UUIDUtils.fromTinkUUID(credentials.getId());
        this.providerName = credentials.getProviderName();
        this.status = status.name();
        this.message = message;
        this.refreshType = getRefreshTypeFromIsManual(isManual).name();

        // Use same timestamp as on the credentials if UPDATED.

        if (Objects.equal(status, CredentialsStatus.UPDATED) && credentials.getUpdated() != null) {
            this.timestamp = credentials.getUpdated();
        }
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public UUID getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getProviderName() {
        return providerName;
    }

    public CredentialsStatus getStatus() {
        return CredentialsStatus.valueOf(status);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status.name();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(UUID timeUUID) {
        this.id = timeUUID;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public RefreshType getRefreshType() {
        if (refreshType == null) {
            return RefreshType.UNKNOWN;
        }

        return RefreshType.valueOf(refreshType);
    }

    public void setRefreshType(String refreshType) {
        this.refreshType = refreshType;
    }

    private RefreshType getRefreshTypeFromIsManual(boolean isManual) {
        if (isManual) {
            return RefreshType.MANUAL;
        }

        return RefreshType.AUTOMATIC;
    }
}
