package se.tink.backend.system.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.core.Credentials;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsStatusRequest {

    public static final int MAX_REFRESH_TIME = 10 * 60 * 1000; // 10 minutes
    
    private Credentials credentials;
    private boolean updateContextTimestamp;
    private String userDeviceId;
    private String userId;
    private boolean isManual;

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
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
}
