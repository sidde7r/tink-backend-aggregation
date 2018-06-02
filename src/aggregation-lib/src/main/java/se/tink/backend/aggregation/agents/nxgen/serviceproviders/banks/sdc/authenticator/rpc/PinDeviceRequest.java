package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PinDeviceRequest {
    private String phoneNumber;
    private String deviceName;
    private String deviceId;
    private String publicKey;

    public PinDeviceRequest setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public PinDeviceRequest setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public PinDeviceRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public PinDeviceRequest setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }
}
