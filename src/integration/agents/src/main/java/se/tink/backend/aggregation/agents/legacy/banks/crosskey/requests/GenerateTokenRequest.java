package se.tink.backend.aggregation.agents.banks.crosskey.requests;

public class GenerateTokenRequest {

    private String deviceId;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return  deviceId;
    }
}
