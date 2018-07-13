package se.tink.backend.aggregation.agents.banks.nordea.v15.model.device;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDeviceResponseData {
    private Map<String, Object> deviceRegistrationToken = Maps.newHashMap();

    public Map<String, Object> getDeviceRegistrationToken() {
        return deviceRegistrationToken;
    }

    public void setDeviceRegistrationToken(Map<String, Object> deviceRegistrationToken) {
        this.deviceRegistrationToken = deviceRegistrationToken;
    }
}
