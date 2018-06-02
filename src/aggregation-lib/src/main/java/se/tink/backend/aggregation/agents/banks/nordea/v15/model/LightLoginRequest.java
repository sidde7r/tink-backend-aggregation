package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import java.util.HashMap;
import java.util.Map;

public class LightLoginRequest {
    private Map<String, Object> userId = new HashMap<String, Object>();
    private Map<String, Object> password = new HashMap<String, Object>();
    private Map<String, Object> type = new HashMap<String, Object>();
    private Map<String, Object> deviceRegistrationToken = new HashMap<String, Object>();
    private Map<String, Object> deviceId = new HashMap<String, Object>();

    public Map<String, Object> getPassword() {
        return password;
    }

    public void setPassword(Map<String, Object> password) {
        this.password = password;
    }

    public Map<String, Object> getType() {
        return type;
    }

    public void setType(Map<String, Object> type) {
        this.type = type;
    }

    public Map<String, Object> getDeviceRegistrationToken() {
        return deviceRegistrationToken;
    }

    public void setDeviceRegistrationToken(Map<String, Object> deviceRegistrationToken) {
        this.deviceRegistrationToken = deviceRegistrationToken;
    }

    public Map<String, Object> getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Map<String, Object> deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getUserId() {
        return userId;
    }

    public void setUserId(Map<String, Object> userId) {
        this.userId = userId;
    }
}
