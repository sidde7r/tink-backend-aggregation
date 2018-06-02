package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PdeviceSignContainer {

    private String deviceInfo;
    private String signature;

    public PdeviceSignContainer setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public PdeviceSignContainer setSignature(String signature) {
        this.signature = signature;
        return this;
    }
}
