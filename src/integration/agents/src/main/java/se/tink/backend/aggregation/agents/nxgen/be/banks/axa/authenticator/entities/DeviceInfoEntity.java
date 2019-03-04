package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class DeviceInfoEntity {
    private String deviceId;
    private boolean jailBrokenOrRooted;
    private String versionNumber;
    private String operatingSystem;
    private String model;
    private String brand;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isJailBrokenOrRooted() {
        return jailBrokenOrRooted;
    }

    public void setJailBrokenOrRooted(boolean jailBrokenOrRooted) {
        this.jailBrokenOrRooted = jailBrokenOrRooted;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
