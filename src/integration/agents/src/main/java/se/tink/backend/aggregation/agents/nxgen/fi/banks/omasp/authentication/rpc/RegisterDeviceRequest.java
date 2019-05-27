package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@JsonObject
public class RegisterDeviceRequest {

    private boolean createIdentityToken = false;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("security_key")
    private SecurityKeyRequestEntity securityKey;

    public RegisterDeviceRequest(String deviceId, String securityKeyValue) {
        this.deviceId = deviceId;
        this.securityKey = new SecurityKeyRequestEntity(securityKeyValue);
    }
}
