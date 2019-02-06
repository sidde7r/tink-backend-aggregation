package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

@JsonObject
public class DeviceEntity {
    private String model;
    private String id;
    private PlatformEntity platform;
    private String brand;

    public DeviceEntity(String deviceId) {
        this.model = "iPhone ";
        this.id = deviceId;
        this.platform = new PlatformEntity();
        this.brand = DeviceProfileConfiguration.IOS_STABLE.getMake();
    }
}
