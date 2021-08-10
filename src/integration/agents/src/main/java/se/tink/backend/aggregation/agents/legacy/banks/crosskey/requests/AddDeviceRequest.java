package se.tink.backend.aggregation.agents.banks.crosskey.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddDeviceRequest {
    private String udId;
    private static final String USER_DEVICE_NAME = "iOS / 10.2";
    private static final String DEVICE_INFO = "iPhone8,1";

    public void setUdId(String udId) {
        this.udId = udId;
    }

    public String getUdId() {
        return udId;
    }

    public String getUserDeviceName() {
        return USER_DEVICE_NAME;
    }

    public String getDeviceInfo() {
        return DEVICE_INFO;
    }
}
