package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Enums;
import se.tink.libraries.credentials.rpc.Credentials;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsStatusRequest {
    private Credentials credentials;
    private boolean updateContextTimestamp;
    private String userDeviceId;
    private String userId;
    private boolean isManual;
    private boolean isMigrationUpdate;
    private String refreshId;
    private CredentialsRequestType credentialsRequestType;

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public boolean isMigrationUpdate() {
        return isMigrationUpdate;
    }

    public void setMigrationUpdate(boolean migrationUpdate) {
        isMigrationUpdate = migrationUpdate;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public boolean isUpdateContextTimestamp() {
        return updateContextTimestamp;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setUpdateContextTimestamp(boolean updateContextTimestamp) {
        this.updateContextTimestamp = updateContextTimestamp;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public void setCredentialsRequestType(
            se.tink.libraries.credentials.service.CredentialsRequestType
                    serviceCredentialsRequestType) {
        if (serviceCredentialsRequestType == null) {
            return;
        }
        String name = serviceCredentialsRequestType.name();
        credentialsRequestType = Enums.getIfPresent(CredentialsRequestType.class, name).orNull();
    }

    public CredentialsRequestType getCredentialsRequestType() {
        return credentialsRequestType;
    }
}
