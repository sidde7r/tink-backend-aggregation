package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;

public class AddDeviceRequest {
    private String udId;
    // These two are directly taken from the users device, so maybe we can make that effort.
    private final String userDeviceName = AlandsBankenConstants.MultiFactorAuthentication.USER_DEVICE_NAME;
    private final String deviceInfo = AlandsBankenConstants.MultiFactorAuthentication.DEVICE_INFO;

    public String getUdId() {
        return udId;
    }

    public AddDeviceRequest setUdId(String udId) {
        this.udId = udId;
        return this;
    }

    public String getUserDeviceName() {
        return userDeviceName;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}
