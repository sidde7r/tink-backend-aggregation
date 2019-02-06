package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

@JsonObject
public class UpdateDeviceRequest {
    private String osVersion;
    private String os;
    private String model;
    private String brand;

    public UpdateDeviceRequest() {
        this.osVersion = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        this.os = DeviceProfileConfiguration.IOS_STABLE.getOs();
        this.model = DeviceProfileConfiguration.IOS_STABLE.getPhoneModel();
        this.brand = DeviceProfileConfiguration.IOS_STABLE.getMake();
    }
}
