package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddDeviceRequest extends SpankkiRequest {
    private String hardwareId;
    private String userDeviceName;
    private String deviceInfo = "";

    public AddDeviceRequest setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
        return this;
    }

    public AddDeviceRequest setUserDeviceName(String userDeviceName) {
        this.userDeviceName = userDeviceName;
        return this;
    }

    public AddDeviceRequest setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }
}
