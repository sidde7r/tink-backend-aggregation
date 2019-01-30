package se.tink.backend.aggregation.agents.banks.nordea.v15.model.device;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDeviceRequestData {
    private Map<String, Object> deviceId = Maps.newHashMap();

    public Map<String, Object> getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Map<String, Object> deviceId) {
        this.deviceId = deviceId;
    }
}
