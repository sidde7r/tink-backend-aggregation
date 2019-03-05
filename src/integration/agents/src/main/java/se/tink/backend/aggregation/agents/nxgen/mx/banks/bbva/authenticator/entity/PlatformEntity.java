package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

@JsonObject
public class PlatformEntity {
    private String id;
    private String version;

    public PlatformEntity() {
        this.id = DeviceProfileConfiguration.IOS_STABLE.getOs();
        this.version = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
    }
}
