package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import java.util.HashMap;
import java.util.Map;

public class LightLoginRequest {
    protected Map<String, Object> password = new HashMap<String, Object>();
    protected Map<String, Object> type = new HashMap<String, Object>();
    protected Map<String, Object> userId = new HashMap<String, Object>();

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

    public Map<String, Object> getUserId() {
        return userId;
    }

    public void setUserId(Map<String, Object> userId) {
        this.userId = userId;
    }
}
