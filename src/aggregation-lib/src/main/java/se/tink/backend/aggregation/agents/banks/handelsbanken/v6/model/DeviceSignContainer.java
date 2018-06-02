package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class DeviceSignContainer {
    private String deviceInfo;
    private String signature;

    public DeviceSignContainer(String deviceInfo, String signature) {
        this.deviceInfo = deviceInfo;
        this.signature = signature;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
