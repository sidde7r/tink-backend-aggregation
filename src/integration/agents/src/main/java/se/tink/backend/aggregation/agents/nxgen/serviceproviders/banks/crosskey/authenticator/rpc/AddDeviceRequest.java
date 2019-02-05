package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddDeviceRequest {
    private String udId;
    // These two are directly taken from the users device, so maybe we can make that effort.
    private final String userDeviceName = CrossKeyConstants.MultiFactorAuthentication.USER_DEVICE_NAME;
    private final String deviceInfo = CrossKeyConstants.MultiFactorAuthentication.DEVICE_INFO;

    public AddDeviceRequest setUdId(String udId) {
        this.udId = udId;
        return this;
    }
}
