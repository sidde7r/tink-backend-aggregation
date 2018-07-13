package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceEntity {
    @JsonProperty("DeviceType")
    private String deviceType;
    @JsonProperty("DeviceSerialNo")
    private String deviceSerialNumber;
    @JsonProperty("FriendlyName")
    private String friendlyName;
    @JsonProperty("PreferredDevice")
    private boolean preferredDevice;
    @JsonProperty("IsDeviceExpired")
    private boolean deviceExpired;

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean isPreferredDevice() {
        return preferredDevice;
    }

    public boolean isDeviceExpired() {
        return deviceExpired;
    }
}
