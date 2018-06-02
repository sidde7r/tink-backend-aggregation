package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsStatusRequest {
    private Credentials credentials;
    private boolean updateContextTimestamp;
    private String userDeviceId;
    private String userId;
    private boolean isManual;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isUpdateContextTimestamp() {
        return updateContextTimestamp;
    }

    public void setUpdateContextTimestamp(boolean updateContextTimestamp) {
        this.updateContextTimestamp = updateContextTimestamp;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }
}
