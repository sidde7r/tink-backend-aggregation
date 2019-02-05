package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JwtPayloadEntity {
    private AppEntity app;
    private CustomEntity custom;
    private DeviceEntity device;
    private String token;
    private String applicationId;
    private String deviceId;

    public void setApp(AppEntity app) {
        this.app = app;
    }

    public void setCustom(CustomEntity custom) {
        this.custom = custom;
    }

    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
