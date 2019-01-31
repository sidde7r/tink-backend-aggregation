package se.tink.backend.aggregation.agents.banks.crosskey.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddDeviceRequest {
    private String udId;
    private final String userDeviceName = "iOS / 10.2";
    private final String deviceInfo = "iPhone8,1";

    public void setUdId(String udId) {
        this.udId = udId;
    }

    public String getUdId() {
        return udId;
    }

    public String getUserDeviceName() {
        return userDeviceName;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}
