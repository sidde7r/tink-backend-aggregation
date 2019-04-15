package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;

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

    public boolean isCodeApp() {
        return DanskeBankConstants.Device.DEVICE_TYPE_CODE_APP.equalsIgnoreCase(deviceType);
    }

    public boolean isOtpCard() {
        return DanskeBankConstants.Device.DEVICE_TYPE_OTP_CARD.equalsIgnoreCase(deviceType);
    }
}
